package org.magnum.dataup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;

 
public class Sending {

	public static void main(String args[]) {
		
		File testVideoData = new File(
				"src/test/resources/test.mp4");
		
		System.out.println("Sending video...");
		String url = "http://localhost:8290/receive";
		
		try {
			
			HttpPost httpPost = new HttpPost(url);
			
			HttpClient client = HttpClientBuilder.create().build();
			InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(testVideoData), -1);
			reqEntity.setContentType("binary/octet-stream");
			//reqEntity.setChunked(true);
			httpPost.setEntity(reqEntity);
			HttpResponse response = client.execute(httpPost);
			
			response.getAllHeaders();
			
			System.out.println("Video sent!");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
	}
	
	
}
