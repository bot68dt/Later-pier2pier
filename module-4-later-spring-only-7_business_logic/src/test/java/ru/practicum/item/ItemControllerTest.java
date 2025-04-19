package ru.practicum.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.config.WebConfig;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.user.User;
import ru.practicum.user.UserState;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringJUnitWebConfig({ ItemController.class, ItemControllerTestConfig.class, WebConfig.class})
class ItemControllerTest {
    private ItemService itemService;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private ItemDto itemDto;

    @Autowired
    ItemControllerTest(ItemService itemService) {
        this.itemService = itemService;
    }


    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();

        itemDto = ItemDto.builder().id(1L)
                .title("Free tool to visualize, parse, analyze and compare URLs. Great for decoding complex url structures.")
                .normalUrl("https://parseurlonline.com/")
                .resolvedUrl("https://parseurlonline.com/")
                .hasImage(false)
                .hasVideo(false)
                .mimeType("text")
                .unread(true)
                .dateResolved(Instant.now())
                .tags(Set.of("Spring"))
                .build();

        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setUrl("https://parseurlonline.com/");
        addItemRequest.setTags(Set.of("Spring"));
    }

    @Test
    void add() throws Exception {
        when(itemService.addNewItem(any(), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")

                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem() throws Exception {
        doNothing().when(itemService).deleteItem(anyLong(), anyLong());

        mvc.perform(delete("/{itemId}", itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void modifyItem() throws Exception {
        when(itemService.changeItem(anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}