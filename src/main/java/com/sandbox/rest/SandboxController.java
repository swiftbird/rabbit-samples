package com.sandbox.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sandbox.model.GenericResponse;
import com.sandbox.model.UsageRequest;
import com.sandbox.services.SandboxService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/async")
public class SandboxController {

	// private MsisdnValidator msisdnValidator;
	// private CustomerActivityService customerActivityService;

	// @Autowired
	// public SandboxController(MsisdnValidator validateMsisdn,
	// CustomerActivityService customerActivityService) {
	// this.msisdnValidator = validateMsisdn;
	// this.customerActivityService = customerActivityService;
	// }

	@Autowired
	private SandboxService sandboxSvc;

	@RequestMapping(value = "/usages/{requestId}", method = RequestMethod.GET)
	@ResponseBody
	public GenericResponse getUsage(@ApiParam(value = "The UUID of the request", required = true) @PathVariable String requestId) throws Exception {

		// this.msisdnValidator.validate(msisdn);

		return sandboxSvc.getFromDynamicQ(requestId);
	}
	
	@RequestMapping(value = "/usagerequests", method = RequestMethod.POST)
	@ResponseBody
	public UsageRequest postUsageRequest(@ApiParam(value = "The UsageRequest for this request", required = true) @RequestBody UsageRequest request) throws Exception {

		// this.msisdnValidator.validate(msisdn);

		return sandboxSvc.enqueueRequest(request);
	}

	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	@ResponseBody
	public GenericResponse ping() {
		return sandboxSvc.ping();
	}

	@RequestMapping(value = "/_startconsumer", method = RequestMethod.GET)
	@ResponseBody
	public GenericResponse startConsumer() throws Exception {
		return sandboxSvc.startConsumer();
	}
	
	@RequestMapping(value = "/_stopconsumer", method = RequestMethod.GET)
	@ResponseBody
	public GenericResponse stopConsumer() throws Exception {
		return sandboxSvc.stopConsumer();
	}
	
	@RequestMapping(value = "/_startpubsubconsumer", method = RequestMethod.GET)
	@ResponseBody
	public GenericResponse startPubSubConsumer() throws Exception {
		return sandboxSvc.startPubSubConsumer();
	}
	
	@ApiOperation(value = "PubSub", notes = "Connect to Rabbit and send a Message for Pub/Sub")
	@RequestMapping(value = "/pubsub", method = RequestMethod.POST)
	public GenericResponse enqueue(@ApiParam(value = "The Message to be enqueued", required = true) @RequestBody String message) throws Exception {

		System.out.println("Going to publish message: " + message);

		return sandboxSvc.sendPubSub(message);
	}
	

}
