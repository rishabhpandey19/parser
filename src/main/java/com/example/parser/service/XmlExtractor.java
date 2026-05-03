package com.example.parser.service;

import com.example.parser.model.FormatType;
import com.example.parser.model.SelectorConfig;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * XML extraction via XPath ({@code //order/@id}).
 *
 * <p>Elements are serialized as JSON-friendly maps: attributes are prefixed with
 * {@code @} (e.g. {@code {"@id":"42"}}), repeated child elements become lists,
 * and leaf text is stored under {@code _text}.
 */
@Component
public class XmlExtractor implements Extractor {

    /**
     * Namespace context that resolves every prefix to the null namespace.
     * This makes XPath prefix-less element names match elements without a
     * namespace, which covers the common (non-namespaced) documents we parse.
     */
    private static final NamespaceContext NO_NAMESPACE = new NamespaceContext() {
        @Override
        public String getPrefix(String uri) {
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String uri) {
            return java.util.Collections.emptyIterator();
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return null;
        }
    };

    @Override
    public FormatType format() {
        return FormatType.XML;
    }

    @Override
    public Map<String, Object> extract(String content, Map<String, SelectorConfig> fields) {
        Node document = parse(content);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(NO_NAMESPACE);

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, SelectorConfig> entry : fields.entrySet()) {
            result.put(entry.getKey(), extractField(xpath, document, entry.getValue()));
        }
        return result;
    }

    private Object extractField(XPath xpath, Node document, SelectorConfig selector) {
        String expr = selector.getPath();
        if (expr == null || expr.isBlank()) {
            return null;
        }
        boolean isAttribute = containsAttributeAxis(expr);
        try {
            if (selector.getMode() == SelectorConfig.Mode.LIST && !isAttribute) {
                NodeList nodes = (NodeList) xpath.evaluate(expr, document, XPathConstants.NODESET);
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < nodes.getLength(); i++) {
                    list.add(toJson(nodes.item(i)));
                }
                return list;
            }
            if (isAttribute) {
                // Attributes yield a string (first match) — empty string if absent.
                String value = xpath.evaluate(expr, document);
                return value == null || value.isEmpty() ? null : value;
            }
            // VALUE / SUBTREE on an element: take the first matching node.
            NodeList nodes = (NodeList) xpath.evaluate(expr, document, XPathConstants.NODESET);
            return nodes.getLength() == 0 ? null : toJson(nodes.item(0));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean containsAttributeAxis(String expr) {
        return expr.contains("/@") || expr.matches("^@.+$") || expr.trim().startsWith("@");
    }

    private Object toJson(Node node) {
        return switch (node.getNodeType()) {
            case Node.ELEMENT_NODE -> elementToMap((Element) node);
            case Node.TEXT_NODE, Node.CDATA_SECTION_NODE ->
                    node.getNodeValue() == null ? "" : node.getNodeValue().strip();
            default -> node.getNodeValue();
        };
    }

    private Object elementToMap(Element element) {
        Map<String, Object> map = new LinkedHashMap<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            map.put("@" + attr.getName(), attr.getValue());
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                mergeChild(map, child.getLocalName(), elementToMap((Element) child));
            }
        }
        // Only attach leaf text when this element carries no child-element data
        // (otherwise getTextContent() would concatenate descendants' text).
        String text = element.getTextContent().strip();
        if (!text.isEmpty() && map.values().stream().allMatch(v -> v instanceof String)) {
            map.put("_text", text);
        }
        return map.isEmpty() ? "" : map;
    }

    private void mergeChild(Map<String, Object> parent, String name, Object value) {
        Object existing = parent.get(name);
        if (existing == null) {
            parent.put(name, value);
        } else if (existing instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) existing;
            list.add(value);
        } else {
            List<Object> list = new ArrayList<>();
            list.add(existing);
            list.add(value);
            parent.put(name, list);
        }
    }

    private Node parse(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // Harden against external entity resolution on untrusted input.
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            factory.setXIncludeAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(content)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse XML: " + e.getMessage(), e);
        }
    }
}
