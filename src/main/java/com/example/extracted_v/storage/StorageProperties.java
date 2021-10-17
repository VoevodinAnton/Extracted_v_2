package com.example.extracted_v.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	private String jsonLocation = "upload-dir-json";
	private String documentLocation = "upload-dir-document";

	public String getJsonLocation() {
		return jsonLocation;
	}

	public String getDocumentLocation(){
		return documentLocation;
	}

	public void setJsonLocation(String location) {
		this.jsonLocation = location;
	}

	public void setDocumentLocation(String location){
		this.documentLocation = location;
	}

}
