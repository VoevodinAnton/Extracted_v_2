package com.example.extracted_v.storage;

import org.apache.http.HttpResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	void doProcess(MultipartFile file);

	Stream<Path> loadAll(Path location);

	Path load(String filename);

	Resource loadAsResource(String filename) throws IOException;

	void deleteAll();

}
