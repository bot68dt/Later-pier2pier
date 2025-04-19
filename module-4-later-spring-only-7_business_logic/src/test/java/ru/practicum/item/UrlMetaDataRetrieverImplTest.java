package ru.practicum.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class UrlMetaDataRetrieverImplTest {

    @InjectMocks
    private static UrlMetaDataRetrieverImpl urlMetaDataRetriever = new UrlMetaDataRetrieverImpl(120);

    @Test
    void retrieve() throws URISyntaxException {


        String urlString = "https://www.freeformatter.com/url-parser-query-string-splitter.html";
        UrlMetaDataRetrieverImpl.UrlMetadataImpl result = new UrlMetaDataRetrieverImpl.UrlMetadataImpl("https://www.freeformatter.com/url-parser-query-string-splitter.html",
                "https://www.freeformatter.com/url-parser-query-string-splitter.html", "text",
                "Free Online Url Parser / Query String Splitter - FreeFormatter.com", true, false, Instant.now());
        result.toBuilder()
                .normalUrl(urlString)
                .resolvedUrl(null)
                .mimeType(MediaType.ALL.getType())
                .dateResolved(Instant.now())
                .build();
        List<UrlMetaDataRetriever.UrlMetadata> resultSource = List.of(result);

        List<UrlMetaDataRetriever.UrlMetadata> urlMetadata = List.of(urlMetaDataRetriever.retrieve(urlString));

        for (UrlMetaDataRetriever.UrlMetadata sourceItem : resultSource) {
            assertThat(urlMetadata, hasItem( allOf(
                    hasProperty("normalUrl", equalTo(sourceItem.getNormalUrl())),
                    hasProperty("resolvedUrl", equalTo(sourceItem.getResolvedUrl())),
                    hasProperty("mimeType", equalTo(sourceItem.getMimeType())),
                    hasProperty("title", equalTo(sourceItem.getTitle())),
                    hasProperty("hasImage", equalTo(sourceItem.isHasImage())),
                    hasProperty("hasVideo", equalTo(sourceItem.isHasVideo()))
            )));
        }
    }

}