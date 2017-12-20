package com.eaybars.webstart.service.artifact.entity;

import com.eaybars.webstart.service.json.control.Enrich;
import com.eaybars.webstart.service.json.entity.JsonSerializable;

import javax.json.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a web start artifact which provides information or downloadable resources about the item it represents.
 * This may be a grouping component, an executable application, some downloadable resources or any other stuff depending
 * on the subclass. An artifact may have sub artifacts inferred from the hierarchy of their identifier URIs.
 */
public class Artifact implements Comparable<Artifact>, JsonSerializable, Serializable {

    static {
        FieldValidation.install(Artifact.class, new HashMap<String, FieldValidator>() {{
            put("identifier", FieldValidator.READ_ONLY);
            put("type", FieldValidator.READ_ONLY);
            put("title", FieldValidator.NON_EMPTY_STRING_VALUE);
            put("icon", FieldValidator.URI_VALUE);
            put("description", FieldValidator.STRING_VALUE);
        }});
    }


    private URI identifier;
    private JsonObject attributes;

    public Artifact(URI identifier) throws URISyntaxException {
        this.identifier = Objects.requireNonNull(identifier);

        String idStr = identifier.toString();
        if ("".equals(idStr) || idStr.charAt(0) != '/') {
            throw new URISyntaxException(identifier.toString(), "Identifier URI must start with a / character", 0);
        }

        if (idStr.charAt(idStr.length() - 1) == '/') {
            int index = idStr.lastIndexOf('/', idStr.length() - 2);
            idStr = idStr.substring(index + 1, idStr.length() - 1);
        } else {
            int startIndex = idStr.lastIndexOf('/');
            int endIndex = idStr.lastIndexOf('.');
            if (endIndex < startIndex) {
                endIndex = idStr.length();
            }
            idStr = idStr.substring(startIndex + 1, endIndex);
        }

        attributes = Json.createObjectBuilder()
                .add("identifier", identifier.toString())
                .add("type", getClass().getSimpleName().toLowerCase())
                .add("title", idStr)
                .build();
    }

    protected void setAttributes(JsonObject attributes) {
        this.attributes = attributes;
    }

    /**
     * Indicates the domain of the artifact and uniquely identifies it. Must start with a "/" character.
     *
     * @return URI identifying the component. May NOT be null
     */
    public URI getIdentifier() {
        return identifier;
    }


    /**
     * Title of the artifact. For example, this may be a title for an application, grouping component or a resource artifact
     *
     * @return title
     */
    public String getTitle() {
        return attributes.getString("title");
    }

    /**
     * Title of the artifact. For example, this may be a title for an application, grouping component or a resource artifact
     *
     * @param title new title
     */
    public void setTitle(String title) {
        attributes().add("title", title).build();
    }

    /**
     * Provides a URI to the icon representation for the artifact.
     *
     * @return icon representation of this artifact
     */
    public URI getIcon() {
        return Optional.ofNullable(attributes.getString("icon", null))
                .map(URI::create)
                .orElse(null);
    }

    /**
     * URI to the icon representation for the artifact.
     *
     * @param icon new icon uri
     */
    public void setIcon(URI icon) {
        if (icon == null) {
            attributes().addNull("icon").build();
        } else {
            attributes().add("icon", icon.toString()).build();
        }
    }

    public JsonObjectBuilder attributes() {
        return new AttributeBuilder(Enrich.object(attributes));
    }

    public final JsonObjectBuilder emptyAttributes() {
        return new AttributeBuilder(baseAttributes());
    }

    protected JsonObjectBuilder baseAttributes() {
        return Json.createObjectBuilder()
                .add("identifier", getIdentifier().toString())
                .add("type", getClass().getSimpleName().toLowerCase())
                .add("title", getTitle());
    }

    @Override
    public int compareTo(Artifact o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artifact that = (Artifact) o;

        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }

    /**
     * Contains arbitrary information alongside some basic ones like identifier, type, title and icon uri. This is the
     * Json representation of the artifact.
     *
     * @return attribute document which is the Json representation of the artifact
     */
    @Override
    public JsonObject toJson() {
        return attributes;
    }

    private class AttributeBuilder implements JsonObjectBuilder {
        private JsonObjectBuilder objectBuilder;

        public AttributeBuilder(JsonObjectBuilder objectBuilder) {
            this.objectBuilder = objectBuilder;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonValue value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, String value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, BigInteger value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, BigDecimal value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, int value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, long value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, double value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, boolean value) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, value);
            objectBuilder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder addNull(String name) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, null);
            objectBuilder.addNull(name);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, builder);
            objectBuilder.add(name, builder);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
            FieldValidation.getValidator(Artifact.this.getClass(), name).accept(name, builder);
            objectBuilder.add(name, builder);
            return this;
        }

        @Override
        public JsonObject build() {
            JsonObject object = objectBuilder.build();
            setAttributes(object);
            return object;
        }
    }
}
