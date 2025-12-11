package com.july.resourceservice.service;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.july.resourceservice.dto.SongMetadataRequest;
import com.july.resourceservice.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class Mp3MetadataExtractor {
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19\\d{2}|20\\d{2})");
    private static final String UNKNOWN = "unknown";
    private final Tika tika = new Tika();

    public SongMetadataRequest extractMetadata(byte[] data, String providedContentType) {
        validateMp3(data, providedContentType);
        try (InputStream stream = new ByteArrayInputStream(data)) {
            Metadata metadata = parseMetadata(stream);
            String title = firstNonBlank(metadata.get(TikaCoreProperties.TITLE), metadata.get("title"));
            String artist = metadata.get(XMPDM.ARTIST);
            String album = metadata.get(XMPDM.ALBUM);
            String duration = formatDuration(metadata.get(XMPDM.DURATION));
            String year = extractYear(metadata.get(XMPDM.RELEASE_DATE));

            if (isBlank(title) || isBlank(artist) || isBlank(album) || isBlank(duration) || isBlank(year)) {
                throw new BadRequestException("MP3 file is missing required tags");
            }
            return new SongMetadataRequest(null, title, artist, album, duration, year);
        } catch (IOException | SAXException e) {
            throw new BadRequestException("Invalid MP3 file");
        } catch (Exception e) {
            throw new BadRequestException("Unable to read MP3 metadata");
        }
    }

    private void validateMp3(byte[] data, String providedContentType) {
        if (isNull(data) || data.length == 0) {
            throw new BadRequestException("MP3 file must not be empty");
        }
        String mediaType = resolveMediaType(data, providedContentType);
        if (isNull(mediaType) || !mediaType.contains("audio/mpeg")) {
            throw new BadRequestException("Invalid file format: " + mediaType + ". Only MP3 files are allowed");
        }
    }

    private String resolveMediaType(byte[] data, String providedContentType) {
        if (isNotBlank(providedContentType)) {
            return providedContentType;
        }
        String detected = tika.detect(data);
        return isBlank(detected) ? UNKNOWN : detected;
    }

    private Metadata parseMetadata(InputStream stream) throws IOException, SAXException {
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        try {
            parser.parse(stream, new BodyContentHandler(), metadata, context);
        } catch (org.apache.tika.exception.TikaException e) {
            throw new BadRequestException("Invalid MP3 file");
        }
        return metadata;
    }

    private String formatDuration(String durationValue) {
        if (isBlank(durationValue)) {
            return null;
        }
        try {
            double seconds = Double.parseDouble(durationValue);
            Duration duration = Duration.ofMillis(Math.round(seconds * 1000));
            return String.format("%02d:%02d", duration.toMinutes(), duration.toSecondsPart());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractYear(String rawYear) {
        if (isBlank(rawYear)) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(rawYear);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }
}
