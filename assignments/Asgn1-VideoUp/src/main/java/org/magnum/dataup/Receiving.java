package org.magnum.dataup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Receiving {

	
	@RequestMapping(value = "/receive", method = RequestMethod.POST)
	public @ResponseBody String receive(HttpServletRequest request, HttpServletResponse response) {
		
		String requestContentType = request.getContentType();
		
		File fileDest = new File("/tmp/transfer.mp4");
		
		try {
			fileDest.createNewFile();
			FileOutputStream f = new FileOutputStream(fileDest);
			IOUtils.copy(request.getInputStream(), f);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			fileDest.delete();
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fileDest.delete();
			e.printStackTrace();
		}
		
		return "OK - " + requestContentType;
	}
	
	
}
