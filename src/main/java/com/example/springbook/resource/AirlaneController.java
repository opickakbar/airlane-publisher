package com.example.springbook.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbook.model.Airlane;
import com.example.springbook.repository.AirlaneRepository;

@RestController
@RequestMapping("airlane")
public class AirlaneController {
	
	@Autowired
	private AirlaneRepository airlanes;
	
	@Autowired
	private KafkaTemplate<String, Airlane> airlaneProducer;
	
	//topics
	private static final String INSERT_TOPIC = "airlane-insert", UPDATE_TOPIC = "airlane-update";
	
	
	@PostMapping("")
	public ResponseEntity<String> createAirlane(@RequestBody Airlane airlane) {	
		//set id
		int id = (int) airlanes.count()+1;
		
		//validation
		if(airlane.getCode().equals(""))
			return new ResponseEntity<>(
			          "Code is required!", 
			          HttpStatus.BAD_REQUEST);
		if(airlane.getName().equals(""))
			return new ResponseEntity<>(
			          "Name is required", 
			          HttpStatus.BAD_REQUEST);
		if(!airlane.getStatus().equals("active") && !airlane.getStatus().equals("inactive"))
			return new ResponseEntity<>(
			          "Status must be between active or inactive", 
			          HttpStatus.BAD_REQUEST);
		
		//insert new airlane
		Airlane newAirlane = new Airlane(id, airlane.getCode(), airlane.getName(), airlane.getStatus());
		airlanes.save(newAirlane);
		
		//publish data to subscribers
		airlaneProducer.send(INSERT_TOPIC, newAirlane);
		return new ResponseEntity<>(
		          "Inserted & published new airlane with id: "+newAirlane.getId(), 
		          HttpStatus.OK);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<String> updateAirlane(@PathVariable int id, @RequestBody Airlane airlane){
		
		//check data exist or not
		if(!airlanes.existsById(id))
			return new ResponseEntity<>(
		          "Data not found", 
		          HttpStatus.OK);
		
		//validation
		if(airlane.getCode().equals(""))
			return new ResponseEntity<>(
			          "Code is required!", 
			          HttpStatus.BAD_REQUEST);
		if(airlane.getName().equals(""))
			return new ResponseEntity<>(
			          "Name is required", 
			          HttpStatus.BAD_REQUEST);
		if(!airlane.getStatus().equals("active") && !airlane.getStatus().equals("inactive"))
			return new ResponseEntity<>(
			          "Status must be between active or inactive", 
			          HttpStatus.BAD_REQUEST);
		
		//update airlane data
		Airlane updatedAirlane = new Airlane(id, airlane.getCode(), airlane.getName(), airlane.getStatus());
		airlanes.deleteById(id);
		airlanes.save(updatedAirlane);
		
		//publish data update
		airlaneProducer.send(UPDATE_TOPIC, updatedAirlane);
		return new ResponseEntity<>(
		          "Update success & published", 
		          HttpStatus.OK);
	}
	
}
