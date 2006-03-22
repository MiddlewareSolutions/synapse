package org.apache.synapse.resources.xml;

import org.apache.synapse.xml.Constants;
import org.apache.synapse.xml.ProcessorConfigurator;
import org.apache.synapse.xml.ProcessorConfiguratorFinder;
import org.apache.synapse.Processor;
import org.apache.synapse.SynapseEnvironment;
import org.apache.synapse.SynapseException;
import org.apache.synapse.processors.ListProcessor;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ResourceMediatorFactory implements ProcessorConfigurator {

    private Log log = LogFactory.getLog(getClass());

    private static final String RESOURCE = "resource";

	private static final QName RESOURCE_Q = new QName(Constants.SYNAPSE_NAMESPACE,
			RESOURCE);

	private static final QName RESOURCE_TYPE_ATT_Q = new QName("type");

	private static final QName RESOURCE_URI_ROOT_ATT_Q = new QName("uri-root");

    public Processor createProcessor(SynapseEnvironment se, OMElement el) {

        ResourceMediator rp = new ResourceMediator();

        this.addPropertiesMediatorsAndURIRoot(se,el,rp);

        OMAttribute type = el.getAttribute(RESOURCE_TYPE_ATT_Q);
		if (type == null) {
			throw new SynapseException(RESOURCE + " must have "
                    + RESOURCE_TYPE_ATT_Q + " attribute: " + el.toString());
		}

		OMAttribute uriRoot = el.getAttribute(RESOURCE_URI_ROOT_ATT_Q);
		if (uriRoot == null) {
			throw new SynapseException(RESOURCE + " must have "
					+ RESOURCE_URI_ROOT_ATT_Q + " attribute: " + el.toString());
		}
        rp.setType(type.getAttributeValue());

        rp.setURIRoot(uriRoot.getAttributeValue());

        return rp;
    }

    public QName getTagQName() {
        return RESOURCE_Q;
    }

    // this methods will give access to SynapseEnvironment's resouces registration


    public void addPropertiesMediatorsAndURIRoot(SynapseEnvironment se, OMElement el,
                                       ListProcessor p) {
        this.setRUIRoot(se,el,p);

        Iterator it = el.getChildElements();
        List processors = new LinkedList();
        while (it.hasNext()) {
            OMElement child = (OMElement) it.next();
            Processor proc =
                    ProcessorConfiguratorFinder.getProcessor(se, child);

            if (proc != null) {
                if (proc instanceof PropertyMediator)
                    processors.add(proc);
                else
                    throw new SynapseException(
                            "List contains an invalid Processsor" +
                                    proc.getClass().getName());
            } else
                log.info("Unknown child of all" + child.getLocalName());
        }
        p.setList(processors);

    }

    public void setRUIRoot(SynapseEnvironment se, OMElement el, Processor p) {

		OMAttribute uriRoot = el.getAttribute(RESOURCE_URI_ROOT_ATT_Q);
		if (uriRoot != null) {
            // uri-root has already set
            se.addResourceProcessor(p);
		}
		log.debug("compile "+el.getLocalName()+" with uri-root '"+p.getName() +"' on "+p.getClass());

	}

}
