/*******************************************************************************
 *  Copyright (c) 2016 GianMaria Romanato
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     GianMaria Romanato - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.facet.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A reader that builds a minimal plan descriptor for a plan file in the workspace. This class is a simplified version
 * of a similar class existing in the Virgo Server runtime. This parser is not able to capture all the features of a
 * plan; it just creates a minimal Plan object which lists the plan name, version and any other referred plan. Other
 * type of child artifacts (e.g. bundles) or plan attributes (e.g. URI) are ignored as they are not relevant for the
 * purpose of deploying workspace plan to the Virgo Runtime Environment.
 *
 */
public final class PlanReader {

    private static final String PLAN = "plan"; //$NON-NLS-1$

    private static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

    private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

    private static final String VERSION_ATTRIBUTE = "version"; //$NON-NLS-1$

    private static final String ARTIFACT_ELEMENT = "artifact"; //$NON-NLS-1$

    private static final String ATTRIBUTE_ELEMENT = "attribute"; //$NON-NLS-1$

    private static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; //$NON-NLS-1$

    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$

    /**
     * Creates a {@link PlanDescriptor} meta-data artifact from an {@link InputStream}
     *
     * @param inputStream from which the plan is to be read
     * @return The plan descriptor (meta-data) from the input stream
     */
    public Plan read(InputStream inputStream) {
        try {
            Document doc = readDocument(inputStream);
            Element element = doc.getDocumentElement();
            return parsePlanElement(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read plan descriptor", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private Document readDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = createDocumentBuilderFactory().newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {

            public void warning(SAXParseException exception) throws SAXException {
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });
        return builder.parse(inputStream);
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
        return factory;
    }

    private Plan parsePlanElement(Element element) {
        String name = element.getAttribute(NAME_ATTRIBUTE);
        Version version = new Version(element.getAttribute(VERSION_ATTRIBUTE));

        Properties attributes = parseAttributes(element);
        List<PlanReference> nestedPlans = parseNestedPlans(element.getElementsByTagName(ARTIFACT_ELEMENT), attributes);

        return new Plan(name, version, nestedPlans);
    }

    private Properties parseAttributes(Element element) {
        Properties result = new Properties();
        NodeList attributeElements = element.getElementsByTagName(ATTRIBUTE_ELEMENT);
        for (int x = 0; x < attributeElements.getLength(); x++) {
            Element attribute = (Element) attributeElements.item(x);

            String name = attribute.getAttribute(NAME_ATTRIBUTE);
            String value = attribute.getAttribute(VALUE_ATTRIBUTE);

            result.put(name, value);
        }
        return result;
    }

    private List<PlanReference> parseNestedPlans(NodeList artifactElements, Properties attributes) {
        List<PlanReference> refs = new ArrayList<PlanReference>();
        for (int i = 0; i < artifactElements.getLength(); i++) {
            Element artifactElement = (Element) artifactElements.item(i);

            String type = artifactElement.getAttribute(TYPE_ATTRIBUTE);
            if (PLAN.equals(type)) {
                String name = artifactElement.getAttribute(NAME_ATTRIBUTE);
                String version = artifactElement.getAttribute(VERSION_ATTRIBUTE);
                if (version != null && !version.isEmpty()) {
                    refs.add(new PlanReference(name, new Version(version)));
                } else {
                    refs.add(new PlanReference(name, null));
                }
            }
        }

        return refs;
    }

}
