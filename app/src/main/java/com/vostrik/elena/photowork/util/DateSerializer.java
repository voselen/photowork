package com.vostrik.elena.photowork.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Elena on 27.05.2018.
 */

public class DateSerializer extends JsonSerializer<Date> {
    String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public void serialize(Date dt, JsonGenerator jsonGen, SerializerProvider serProv)
            throws IOException, JsonProcessingException {
        DateFormat sdf = new SimpleDateFormat(pattern);
        String formattedDate = sdf.format(dt);
        jsonGen.writeString(formattedDate);
    }
}
