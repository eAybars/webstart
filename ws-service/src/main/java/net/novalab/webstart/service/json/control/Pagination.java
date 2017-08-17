package net.novalab.webstart.service.json.control;

import net.novalab.webstart.service.json.entity.JsonSerializable;

import javax.json.*;
import java.util.List;
import java.util.function.Function;

public class Pagination<T> {

    private Function<T, JsonValue> mapperFunction;
    private List<T> elements;
    private int start;
    private int size;

    private Pagination(Function<T, JsonValue> mapperFunction, List<T> elements) {
        this.mapperFunction = mapperFunction;
        this.elements = elements;
        this.size = elements.size();
    }

    public Pagination<T> startingFrom(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("Start must be zero or a positive number");
        }
        this.start = start;
        return this;
    }

    public Pagination<T> withSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be zero or a positive number");
        }
        this.size = size;
        return this;
    }

    public JsonObject done() {
        int start = Math.min(elements.size(), this.start);
        int end = Math.min(elements.size(), start + size);
        List<T> list = elements.subList(start, end);
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("start", start)
                .add("end", end)
                .add("size", end - start)
                .add("total", this.elements.size());
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        list.stream().map(mapperFunction).forEach(arrayBuilder::add);
        return builder.add("elements", arrayBuilder).build();
    }

    public static <T extends JsonSerializable> Pagination<T> of(List<T> list) {
        return new Pagination<>(T::toJson, list);
    }

    public static <T> PaginationBuilder<T> withMapper(Function<T, JsonValue> mapper) {
        return new PaginationBuilder<>(mapper);
    }


    public static class PaginationBuilder<T> {
        private Function<T, JsonValue> mapper;

        public PaginationBuilder(Function<T, JsonValue> mapper) {
            this.mapper = mapper;
        }

        public Pagination<T> of(List<T> list) {
            return new Pagination<>(mapper, list);
        }
    }

}
