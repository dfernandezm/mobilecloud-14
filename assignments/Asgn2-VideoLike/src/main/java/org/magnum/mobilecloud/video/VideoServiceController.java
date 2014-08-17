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
package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VideoServiceController {

	private Map<Long,List<String>> videosLikes = new HashMap<Long, List<String>>();
	
	@Autowired
	private VideoRepository videoRepository;
	
//	@RequestMapping(value = "/oauth/token", method = RequestMethod.POST)
//	public String getOAuthToken(HttpServletRequest request, HttpServletResponse response) {
//		
//		return null;
//	}
		
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody Iterable<Video> getVideos() {
		Iterable<Video> videos = videoRepository.findAll();
		return videos;
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video saveVideo(@RequestBody Video video) {
		video.setLikes(0L);
		videoRepository.save(video);
		return video;
	}
	
	@RequestMapping(value = "/video/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideo(@PathVariable("id") long videoId, HttpServletResponse response) throws IOException {
		
	    Video video = videoRepository.findOne(videoId);
		
	    if(video != null) {
	    	return video;
	    } else {
	    	response.sendError(HttpServletResponse.SC_NOT_FOUND);
	    	return null;
	    }
	}
	
	@RequestMapping(value = "/video/{id}/like", method = RequestMethod.POST)
	public @ResponseBody Video likeVideo(@PathVariable("id") long videoId, Principal principal, HttpServletResponse response) throws IOException {
		
		Video video = videoRepository.findOne(videoId);
		String username = principal.getName();
		
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		} else if (usernameHasAlreadyLikedVideo(videoId, username)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		} else {
			video.setLikes(video.getLikes()+1);
			videoRepository.save(video);
			saveVideoLike(videoId, username);
		}
		
		return video;
	}
	
	@RequestMapping(value = "/video/{id}/unlike", method = RequestMethod.POST)
	public @ResponseBody Video unlikeVideo(@PathVariable("id") long videoId, Principal principal, HttpServletResponse response) throws IOException {
		
		Video video = videoRepository.findOne(videoId);
		String username = principal.getName();
		
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		} else if (usernameHasAlreadyLikedVideo(videoId, username)){
			video.setLikes(video.getLikes()-1);
			videoRepository.save(video);
			removeVideoLike(videoId, username);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
			
		return video;
	}
	
	@RequestMapping(value = "/video/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody List<String> likedBy(@PathVariable("id") long videoId, HttpServletResponse response) throws IOException {
		
		Video video = videoRepository.findOne(videoId);
		
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		} else {
			return videosLikes.get(videoId);
		}
	}
	
	@RequestMapping(value = "/video/search/findByName", method = RequestMethod.GET)
	public @ResponseBody List<Video> findByName(@RequestParam("title") String title, HttpServletResponse response) throws IOException {
		
		if (StringUtils.isEmpty(title)) {
			return new ArrayList<Video>();
		} else {
			Collection<Video> videos = videoRepository.findByName(title);
			
			if (videos == null || videos.isEmpty()) {
				return new ArrayList<Video>();
			} else {
				return new ArrayList<Video>(videos);
			}
		}
	}
	
	@RequestMapping(value = "/video/search/findByDurationLessThan", method = RequestMethod.GET)
	public @ResponseBody List<Video> findByDurationLessThan(@RequestParam("duration") Long duration, HttpServletResponse response) throws IOException {
		
		if (duration == null) {
			return new ArrayList<Video>();
		} else {
			Collection<Video> videos = videoRepository.findByDurationLessThan(duration);
			
			if (videos == null || videos.isEmpty()) {
				return new ArrayList<Video>();
			} else {
				return new ArrayList<Video>(videos);
			}
		}
	}	
		
	// -- Helper Methods -- 

	private boolean usernameHasAlreadyLikedVideo(Long videoId, String username) {
		List<String> usernames = videosLikes.get(videoId);
		return usernames != null && usernames.contains(username);
	}
	
	private void saveVideoLike(Long videoId, String username) {
		List<String> usernames = videosLikes.get(videoId);
		if (usernames == null) {
			usernames = new ArrayList<String>();	
		}
		usernames.add(username);
		videosLikes.put(videoId, usernames);
	}
	
	private void removeVideoLike(Long videoId, String username) {
		List<String> usernames = videosLikes.get(videoId);
		if (usernames != null) {
			usernames.remove(username);
			videosLikes.put(videoId, usernames);
		}
	}
	
	
//    public Video saveWithDataUrl(Video entity) {
//        checkAndSetId(entity);
//        entity.setDataUrl(getDataUrl(entity.getId()));
//        videos.put(entity.getId(), entity);
//        return entity;
//    }

//    private void checkAndSetId(Video entity) {
//        if (entity.getId() == 0) {
//            entity.setId(currentId.incrementAndGet());
//        }
//    }
//	
//	private String getDataUrl(long videoId){
//        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
//        return url;
//    }
//
//    private String getUrlBaseForLocalServer() {
//       HttpServletRequest request = 
//           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//       String base = 
//          "http://"+request.getServerName() 
//          + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
//       return base;
//    }
	
}
