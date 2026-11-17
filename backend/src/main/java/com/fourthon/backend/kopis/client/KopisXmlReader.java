package com.fourthon.backend.kopis.client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class KopisXmlReader {

    private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";

    private KopisXmlReader() {
    }

    static Document parse(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new KopisException("KOPIS 응답이 비어 있습니다");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(DISALLOW_DOCTYPE, true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            return builder.parse(new InputSource(new ByteArrayInputStream(xml)));
        } catch (Exception e) {
            throw new KopisException("KOPIS 응답을 읽을 수 없습니다", e);
        }
    }

    static List<Element> elements(Document document, String tag) {
        NodeList nodes = document.getElementsByTagName(tag);
        List<Element> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add((Element) nodes.item(i));
        }
        return result;
    }

    static List<Element> children(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && tag.equals(element.getTagName())) {
                result.add(element);
            }
        }
        return result;
    }

    static Optional<Element> child(Element parent, String tag) {
        return children(parent, tag).stream().findFirst();
    }

    static String text(Element parent, String tag) {
        return child(parent, tag)
                .map(Element::getTextContent)
                .map(String::trim)
                .orElse("");
    }
}
