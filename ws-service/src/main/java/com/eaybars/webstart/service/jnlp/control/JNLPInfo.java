package com.eaybars.webstart.service.jnlp.control;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class JNLPInfo {
    private Element root;
    private Element information;

    private JNLPInfo(Element root) {
        this.root = root;
        NodeList infoElements = root.getElementsByTagName("information");
        if (infoElements != null && infoElements.getLength() > 0) {
            information = (Element) infoElements.item(0);
        }
    }

    public static JNLPInfo from(URL url) throws IOException, SAXException {
        try (InputStream is = url.openStream()){
            return from(is);
        }
    }

    public static JNLPInfo from(InputStream is) throws IOException, SAXException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();

            Element element = document.getDocumentElement();
            return new JNLPInfo(element);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getVersion() {
        return root.getAttribute("version");
    }

    public Optional<String> getTitle() {
        return infoElement("title");
    }

    public Optional<String> getVendor() {
        return infoElement("vendor");
    }

    public Optional<String> getHomepage() {
        return infoElement("homepage");
    }

    public Optional<String> getDescription() {
        return infoElement("description");
    }

    public Optional<URI> getIcon() {
        return Stream.of("default", "shortcut", "selected", "splash")
                .map(this::findIconOfKind)
                .filter(Objects::nonNull)
                .findFirst()
                .map(URI::create);
    }

    private String findIconOfKind(String kind) {
        NodeList iconList = information.getElementsByTagName("icon");
        if (iconList != null && iconList.getLength() > 0) {
            for (int i = 0; i < iconList.getLength(); i++) {
                Element element = (Element) iconList.item(i);
                if (kind.equals(element.getAttribute("kind"))) {
                    return element.getAttribute("href");
                }
            }
        }
        return null;
    }

    private Optional<String> infoElement(String elementName) {
        return Optional.ofNullable(information)
                .map(i -> i.getElementsByTagName(elementName))
                .map(nl -> nl.getLength() > 0 ? nl.item(0) : null)
                .map(Element.class::cast)
                .map(Element::getTextContent);
    }

}
