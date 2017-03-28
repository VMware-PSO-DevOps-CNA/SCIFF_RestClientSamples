/*
 * Copyright (c) 2017 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.pso.samples.controller;


import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.o11n.sdk.rest.client.DefaultVcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.VcoSession;
import com.vmware.o11n.sdk.rest.client.services.BuilderUtils;
import com.vmware.o11n.sdk.rest.client.services.CatalogService;
import com.vmware.o11n.sdk.rest.client.services.ExecutionContextBuilder;
import com.vmware.o11n.sdk.rest.client.services.ExecutionService;
import com.vmware.o11n.sdk.rest.client.services.WorkflowService;
import com.vmware.o11n.sdk.rest.client.stubs.ExecutionContext;
import com.vmware.o11n.sdk.rest.client.stubs.Linkable;
import com.vmware.o11n.sdk.rest.client.stubs.Parameter;
import com.vmware.o11n.sdk.rest.client.stubs.SdkObject;
import com.vmware.o11n.sdk.rest.client.stubs.Workflow;
import com.vmware.o11n.sdk.rest.client.stubs.WorkflowExecution;
import com.vmware.o11n.sdk.rest.client.stubs.WorkflowExecutionState;


@RestController
@RequestMapping("/api/vro")
public class VroController {

    // TODO - CHANGE these to workflow UUIDs in your vRO!!!
    private static final String GET_CATALOG_BY_NAME_WF = "ff840fb0-b931-42f6-ba63-fa0521e24bd5";
    private static final String REQ_CATALOG_ITEM_WF = "50c6ad5a-f861-4dd1-8c8a-44d0cd2c613a";
    private static final String GET_CATALOG_RESOURCE_BY_REQUEST_ID = "adfbb640-78ec-44e8-842d-72c0a4bde7e2";
    private static final String GET_CATALOG_RESOURCE_ACTION_BY_NAME_WF = "1851c6b8-5822-4f4e-b8ac-5becf677e49a";
    private static final String REQ_CATALOG_RESOURCE_ACTION_WF = "c54d08db-6538-4b26-96a6-897dad113e73";

    // VCAC host with name of VCAHOST
    private static final String VCAC_HOST = "8037e0db-7e7f-4e01-85b2-cf0122ab4906";

    @RequestMapping(value="/requestCatalogItem", method = RequestMethod.GET, produces = "application/json")
    final public @ResponseBody String requestCatalogItem(@RequestParam String name) throws URISyntaxException, InterruptedException {

        // TODO - CHANGE this to your url, username, and password for vRO!!!
        //start a new session to Orchestrator by using specified credentials
        final VcoSession session = DefaultVcoSessionFactory.newLdapSession(
                new URI("https://<URL_TO_VRO>:8281/vco/api/"), "<Service_Acct_Username>", "<Service_Acct_Password>");

        final SdkObject catalogItem = getCatalogItemByName(session,name);

        final String requestId = requestCatalogItem(session, catalogItem);

        return "CatalogItem " + name + " requested with requestId: " + requestId;
    }

    @RequestMapping(value="/requestCatalogResourceAction", method = RequestMethod.GET, produces = "application/json")
    final public @ResponseBody String requestCatalogItem(@RequestParam String catalogResourceRequestId,
            @RequestParam String catalogResourceActionName) throws URISyntaxException, InterruptedException {
        // TODO - CHANGE this to your url, username, and password for vRO!!!
        //start a new session to Orchestrator by using specified credentials
        final VcoSession session = DefaultVcoSessionFactory.newLdapSession(
                new URI("https://<URL_TO_VRO>:8281/vco/api/"), "<Service_Acct_Username>", "<Service_Acct_Password>");

        final SdkObject catalogResource = getCatalogResourceByRequestId(session, catalogResourceRequestId);

        final SdkObject catalogResourceAction = getCatalogResourceActionByName(session, catalogResource,
                catalogResourceActionName);

        System.out.println("catalogResource " + catalogResource + " catalogResourceAction " + catalogResourceAction );

        requestResourceAction(session, catalogResource, catalogResourceAction);

        return "Requested action " + catalogResourceActionName + " on catalog request id " + catalogResourceRequestId;
    }

    private SdkObject getCatalogItemByName(final VcoSession session, final String name) throws InterruptedException {
        //create the services
        final CatalogService catalogService = new CatalogService(session);
        final WorkflowService workflowService = new WorkflowService(session);
        final ExecutionService executionService = new ExecutionService(session);

        final Workflow workflow = workflowService.getWorkflow(GET_CATALOG_BY_NAME_WF);

        final Linkable vcacHost =
                catalogService.getPluginObjectById("vCACCAFE", "VCACHOST", VCAC_HOST, null);

        // set up parameters
        final ExecutionContext context = new ExecutionContextBuilder()
                .addParam("host", BuilderUtils.newSdkObject(vcacHost))
                .addParam("name",name)
                .build();

        //run the workflow
        WorkflowExecution execution = executionService.execute(workflow, context);

        //wait for the workflow
        execution = executionService.awaitState(execution, 5000, 10, WorkflowExecutionState.CANCELED,
                WorkflowExecutionState.FAILED, WorkflowExecutionState.COMPLETED);
        System.out.println(ToStringBuilder.reflectionToString(execution));


        throw new IllegalArgumentException("Boop something is wrong!");
    }

    private SdkObject getCatalogResourceByRequestId(final VcoSession session, final String requestId)
            throws InterruptedException {
        //create the services
        final CatalogService catalogService = new CatalogService(session);
        final WorkflowService workflowService = new WorkflowService(session);
        final ExecutionService executionService = new ExecutionService(session);

        final Workflow workflow = workflowService.getWorkflow(GET_CATALOG_RESOURCE_BY_REQUEST_ID);

        final Linkable vcacHost =
                catalogService.getPluginObjectById("vCACCAFE", "VCACHOST", VCAC_HOST, null);

        // set up parameters
        final ExecutionContext context = new ExecutionContextBuilder()
                .addParam("host", BuilderUtils.newSdkObject(vcacHost))
                .addParam("requestId", requestId)
                .build();
        //run the workflow
        WorkflowExecution execution = executionService.execute(workflow, context);

        //wait for the workflow
        execution = executionService.awaitState(execution, 5000, 10, WorkflowExecutionState.CANCELED,
                WorkflowExecutionState.FAILED, WorkflowExecutionState.COMPLETED);
        System.out.println(ToStringBuilder.reflectionToString(execution));

        // This is bad... I know but for demo I know there is only one parameter and we want to return that
        for (Parameter param : execution.getOutputParameters().getParameter()) {
            System.out.println("CatalogResource " + param.getName() + " " + param.getType());
            return param.getSdkObject();
        }

        throw new IllegalArgumentException("Boop something is wrong!");
    }


    private SdkObject getCatalogResourceActionByName(final VcoSession session, final SdkObject catalogResource,
            final String actionName) throws InterruptedException {
        //create the services
        final WorkflowService workflowService = new WorkflowService(session);
        final ExecutionService executionService = new ExecutionService(session);

        final Workflow workflow = workflowService.getWorkflow(GET_CATALOG_RESOURCE_ACTION_BY_NAME_WF);

        // set up parameters
        final ExecutionContext context = new ExecutionContextBuilder()
                .addParam("catalogResource", catalogResource)
                .addParam("catalogResourceActionName", actionName)
                .build();

        //run the workflow
        WorkflowExecution execution = executionService.execute(workflow, context);

        //wait for the workflow
        execution = executionService.awaitState(execution, 5000, 10, WorkflowExecutionState.CANCELED,
                WorkflowExecutionState.FAILED, WorkflowExecutionState.COMPLETED);
        System.out.println(ToStringBuilder.reflectionToString(execution));

        // This is bad... I know but for demo I know there is only one parameter and we want to return that
        for (Parameter param : execution.getOutputParameters().getParameter()) {
            System.out.println("CatalogResourceAction " + param.getName() + " " + param.getType());
            return param.getSdkObject();
        }

        throw new IllegalArgumentException("Boop something is wrong!");
    }

    private String requestCatalogItem(final VcoSession session, final SdkObject catalogItem)
            throws InterruptedException {

        //create the services
        final WorkflowService workflowService = new WorkflowService(session);
        final ExecutionService executionService = new ExecutionService(session);

        final Workflow workflow = workflowService.getWorkflow(REQ_CATALOG_ITEM_WF);

        // set up parameters
        final ExecutionContext context = new ExecutionContextBuilder()
                .addParam("item", catalogItem)
                .addParam("inputs","")
                .build();

        //run the workflow
        WorkflowExecution execution = executionService.execute(workflow, context);

        //wait for the workflow
        execution = executionService.awaitState(execution, 5000, 10, WorkflowExecutionState.CANCELED,
                WorkflowExecutionState.FAILED, WorkflowExecutionState.COMPLETED);
        System.out.println(ToStringBuilder.reflectionToString(execution));

        String requestId = "";
        for (Parameter param : execution.getOutputParameters().getParameter()) {
            System.out.println("Request " + param.getName() + " " + param.getType());
            //System.out.println(ToStringBuilder.reflectionToString(param));
            requestId = param.getSdkObject().getId();
            System.out.println("Request Id: " + requestId);
        }

        return requestId;
    }

    private String requestResourceAction(final VcoSession session, final SdkObject catalogResource,
            final SdkObject catalogResourceOperation) throws InterruptedException {

        //create the services
        final WorkflowService workflowService = new WorkflowService(session);
        final ExecutionService executionService = new ExecutionService(session);

        final Workflow workflow = workflowService.getWorkflow(REQ_CATALOG_RESOURCE_ACTION_WF);

        // set up parameters
        final ExecutionContext context = new ExecutionContextBuilder()
                .addParam("resource", catalogResource)
                .addParam("operation", catalogResourceOperation)
                .addParam("inputs","")
                .build();

        //run the workflow
        WorkflowExecution execution = executionService.execute(workflow, context);

        //wait for the workflow
        execution = executionService.awaitState(execution, 5000, 10, WorkflowExecutionState.CANCELED,
                WorkflowExecutionState.FAILED, WorkflowExecutionState.COMPLETED);
        System.out.println(ToStringBuilder.reflectionToString(execution));

        return "Catalog resource action submitted ";
    }
}
