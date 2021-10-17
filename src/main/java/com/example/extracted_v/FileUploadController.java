package com.example.extracted_v;

import com.example.extracted_v.storage.StorageFileNotFoundException;
import com.example.extracted_v.storage.StorageProperties;
import com.example.extracted_v.storage.StorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class FileUploadController {
    private final StorageService storageService;
    private final Path rootDocumentLocation;
    private final Path rootJsonLocation;

    @Autowired
    public FileUploadController(StorageService storageService, StorageProperties properties) {
        this.storageService = storageService;
        this.rootDocumentLocation = Paths.get(properties.getDocumentLocation());
        this.rootJsonLocation = Paths.get(properties.getJsonLocation());
    }

    @GetMapping("/")
    public String getIndexPage(Model model) throws IOException {

        List<List<Map<String, Object>>> listOfProducts = new ArrayList<>();

        listOfProducts = storageService.loadAll(rootJsonLocation).map(path ->
                {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(new File(rootJsonLocation + "\\" + path.toString()), new TypeReference<List<Map<String, Object>>>() {
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
        ).collect(Collectors.toList());
        //List<Map<String, Object>> results = mapper.readValue(new File(path.getFileName().toString()), new TypeReference<List<Map<String, Object>>>() {
        //});
        List<String> paths = storageService.loadAll(rootDocumentLocation).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result = zipToMap(paths, listOfProducts);

        model.addAttribute("products", result);



        for (String path : paths) {
            System.out.println("path: " + path);
        }


        /*
        model.addAttribute("files", storageService.loadAll(rootDocumentLocation).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));


        model.addAttribute("listOfProducts", listOfProducts);

         */

        return "html/uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws IOException {

        storageService.doProcess(file);

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    public static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        System.out.println(keys.size());
        System.out.println(values.size());
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(keys::get, values::get));
    }
}
