package ir.mehradn.rollback.util.gson;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    public static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_')
        .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

    @Override
    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), TIME_FORMATTER);
    }

    @Override
    public JsonElement serialize(LocalDateTime obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(obj.format(TIME_FORMATTER));
    }
}
