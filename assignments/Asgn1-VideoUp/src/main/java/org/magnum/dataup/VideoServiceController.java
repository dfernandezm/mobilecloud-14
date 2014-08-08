/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoServiceController {

	private static final AtomicLong currentId = new AtomicLong(0L);
	private Map<Long,Video> videos = new HashMap<Long, Video>();
	
	
    
	
	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideos() {
		
		return videos.values();
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video) {
		
		Video storedVideo = saveWithDataUrl(video);
		return storedVideo;
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
	public @ResponseBody VideoStatus addMediaToVideo(@PathVariable("id") long videoId, 
			@RequestParam("data") MultipartFile mediaFile, HttpServletResponse response) throws IOException {
		
		VideoStatus videoStatus = new VideoStatus(VideoState.READY);
		
		if (videoId != 0 && mediaFile != null) {
			Video video = videos.get(videoId);
			
			if (video == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return null;
			}
			
			try {
				VideoFileManager.get().saveVideoData(video, mediaFile.getInputStream());
			} catch (IOException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		return videoStatus;
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
	public void getMediaFromVideo(@PathVariable("id") long videoId, HttpServletResponse response) throws IOException {
		
		VideoFileManager videoFileManager = VideoFileManager.get();
		
		Video video = videos.get(videoId);
		
		if (video != null && videoFileManager.hasVideoData(video)) {		
			try {
				videoFileManager.copyVideoData(video, response.getOutputStream());
			} catch (IOException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	
	
	
		
	// -- Helper Methods -- 

    public Video saveWithDataUrl(Video entity) {
        checkAndSetId(entity);
        entity.setDataUrl(getDataUrl(entity.getId()));
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if (entity.getId() == 0) {
            entity.setId(currentId.incrementAndGet());
        }
    }
	
	private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
       HttpServletRequest request = 
           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String base = 
          "http://"+request.getServerName() 
          + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
       return base;
    }
	
}
