package com.example.extracted_v.storage;

import com.example.extracted_v.storage.StorageFileNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootJsonLocation;
    private final Path rootDocumentLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootJsonLocation = Paths.get(properties.getJsonLocation());
        this.rootDocumentLocation = Paths.get(properties.getDocumentLocation());
    }

    @Override
    public void doProcess(MultipartFile file) {
        HttpResponse response;
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }

            Files.copy(file.getInputStream(), this.rootDocumentLocation.resolve(file.getOriginalFilename()));
			/*
			HttpEntity entity = MultipartEntityBuilder.create()
					.addPart("file", new FileBody(file))
					.build();

			HttpPost request = new HttpPost(url);
			request.setEntity(entity);

			HttpClient client = HttpClientBuilder.create().build();
			response = client.execute(request);

			 */
            HttpClient client = new DefaultHttpClient();

            HttpGet get = new HttpGet("http://localhost:5000/process?filePath=" + file.getOriginalFilename());
//            HttpParams params = new BasicHttpParams();
//            params.setParameter("filePath", file.getOriginalFilename());
            System.out.println("file:" + file.getOriginalFilename());
//            get.setParams(params);
//
            HttpResponse response1;
            response1 = client.execute(get);

            String str;
            StringBuilder sb = new StringBuilder();
            HttpEntity entity = response1.getEntity();
            if (entity != null) {
                DataInputStream in;
                in = new DataInputStream(entity.getContent());
                while ((str = in.readLine()) != null) {
                    sb.append(str);
                }
                in.close();
            }

            System.out.println(sb);

            JSONArray jsonArray = new JSONArray("[{\"product\": \"milk\", \"unit\": \"л\", \"number\": 3, \"price\": 12, \"delivery region\": \"Samara\"}, {\"product\": \"laptop\", \"unit\": \"item\", \"number\": 1, \"price\": 120000, \"delivery region\": \"Samara\"}]");


            FileWriter jsonFile = new FileWriter(String.valueOf(this.rootJsonLocation.resolve(file.getName() + ".json")));
            System.out.println("jsonFile: " + this.rootJsonLocation.resolve(file.getName()));
            jsonFile.write(jsonArray.toString());

            jsonFile.flush();
            jsonFile.close();

        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll(Path location) {
        try {
            return Files.walk(location, 1)
                    .filter(path -> !path.equals(location))
                    .map(path -> location.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootDocumentLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) throws IOException {
        try {
            Path file = load(filename);
            System.out.println("file: " + file.toString());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                System.out.println("resource: " + resource.getURL());
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootJsonLocation.toFile());
        FileSystemUtils.deleteRecursively(rootDocumentLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootJsonLocation);
            Files.createDirectories(rootDocumentLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
