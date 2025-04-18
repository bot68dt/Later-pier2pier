package ru.practicum.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.config.AppConfig;
import ru.practicum.config.PersistenceConfig;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.GetItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;
import ru.practicum.item.model.Item;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;
import ru.practicum.user.UserState;

import java.time.*;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(properties = { "jdbc.url=jdbc:postgresql://localhost:5432/later"})
@SpringJUnitConfig( {AppConfig.class, PersistenceConfig.class, ItemServiceImpl.class, UrlMetaDataRetrieverImpl.class, UserRepository.class})
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemService service;
    private final UserRepository userRepository;

    @Test
    void getItems() {

        List<ItemDto> sourceItems = List.of(
                ItemDto.builder().id(1L)
                        .title("title")
                        .normalUrl("normalUrl")
                        .resolvedUrl("ResolvedUrl")
                        .hasImage(false)
                        .hasVideo(false)
                        .mimeType("MimeType")
                        .unread(true)
                        .dateResolved(Instant.now())
                        .tags(Set.of("Spring"))
                        .build(),
                ItemDto.builder().id(2L)
                        .title("title")
                        .normalUrl("normalUrl")
                        .resolvedUrl("ResolvedUrl")
                        .hasImage(false)
                        .hasVideo(false)
                        .mimeType("MimeType")
                        .unread(true)
                        .dateResolved(Instant.now())
                        .tags(Set.of("Spring"))
                        .build());

        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        user = userRepository.saveAndFlush(user);

        for (ItemDto item : sourceItems) {
            Item entity = new Item();
            entity.setUser(user);
            entity.setUrl(item.getNormalUrl());
            entity.setResolvedUrl(item.getResolvedUrl());
            entity.setMimeType(item.getMimeType());
            entity.setTitle(item.getTitle());
            entity.setHasImage(item.isHasImage());
            entity.setHasVideo(item.isHasVideo());
            entity.setDateResolved(item.getDateResolved());
            entity.setTags(item.getTags());
            em.persist(entity);
        }
        em.flush();

        List<ItemDto> targetItems = service.getItems(user.getId());

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem( allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("normalUrl", equalTo(sourceItem.getNormalUrl())),
                    hasProperty("resolvedUrl", equalTo(sourceItem.getResolvedUrl())),
                    hasProperty("mimeType", equalTo(sourceItem.getMimeType())),
                    hasProperty("title", equalTo(sourceItem.getTitle())),
                    hasProperty("hasImage", equalTo(sourceItem.isHasImage())),
                    hasProperty("hasVideo", equalTo(sourceItem.isHasVideo())),
                    hasProperty("unread", equalTo(sourceItem.isUnread())),
                    hasProperty("dateResolved", equalTo(sourceItem.getDateResolved())),
                    hasProperty("tags", equalTo(sourceItem.getTags()))
            )));
        }
    }

    @Test
    void addNewItem() {
        List<ItemDto> sourceItems = List.of(
        ItemDto.builder().id(1L)
                .title("Free tool to visualize, parse, analyze and compare URLs. Great for decoding complex url structures.")
                .normalUrl("https://parseurlonline.com/")
                .resolvedUrl("https://parseurlonline.com/")
                .hasImage(false)
                .hasVideo(false)
                .mimeType("text")
                .unread(true)
                .dateResolved(Instant.now())
                .tags(Set.of("Spring"))
                .build());

        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        user = userRepository.saveAndFlush(user);

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setTags(Set.of("Spring"));
        addItemRequest.setUrl("https://parseurlonline.com/");

        List<ItemDto> targetItemDto = List.of(service.addNewItem(user.getId(), addItemRequest));

        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItemDto, hasItem( allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("normalUrl", equalTo(sourceItem.getNormalUrl())),
                    hasProperty("resolvedUrl", equalTo(sourceItem.getResolvedUrl())),
                    hasProperty("mimeType", equalTo(sourceItem.getMimeType())),
                    hasProperty("title", equalTo(sourceItem.getTitle())),
                    hasProperty("hasImage", equalTo(sourceItem.isHasImage())),
                    hasProperty("hasVideo", equalTo(sourceItem.isHasVideo())),
                    hasProperty("unread", equalTo(sourceItem.isUnread())),
                    //hasProperty("dateResolved", equalTo(sourceItem.getDateResolved())), - есть задержка при парсинге контента. не проверяем
                    hasProperty("tags", equalTo(sourceItem.getTags()))
            )));
        }
    }

    @Test
    void deleteItem() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        user = userRepository.saveAndFlush(user);

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setTags(Set.of("Spring"));
        addItemRequest.setUrl("https://parseurlonline.com/");

        ItemDto itemDto = service.addNewItem(user.getId(), addItemRequest);
        service.deleteItem(user.getId(), itemDto.getId());

        Assertions.assertTrue(service.getItems(user.getId()).isEmpty());
    }

    @Test
    void testGetItems() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        user = userRepository.saveAndFlush(user);

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setTags(Set.of("Spring"));
        addItemRequest.setUrl("https://parseurlonline.com/");

        GetItemRequest getItemRequest = GetItemRequest.of(user.getId(), GetItemRequest.State.ALL.name(),
                GetItemRequest.ContentType.ALL.name(), GetItemRequest.Sort.NEWEST.name(), 5000, List.of("Spring"));

        List<ItemDto> sourceItems = List.of(service.addNewItem(user.getId(), addItemRequest));
        List<ItemDto> targetItemDto = service.getItems(getItemRequest);

        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItemDto, hasItem( allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("normalUrl", equalTo(sourceItem.getNormalUrl())),
                    hasProperty("resolvedUrl", equalTo(sourceItem.getResolvedUrl())),
                    hasProperty("mimeType", equalTo(sourceItem.getMimeType())),
                    hasProperty("title", equalTo(sourceItem.getTitle())),
                    hasProperty("hasImage", equalTo(sourceItem.isHasImage())),
                    hasProperty("hasVideo", equalTo(sourceItem.isHasVideo())),
                    hasProperty("unread", equalTo(sourceItem.isUnread())),
                    //hasProperty("dateResolved", equalTo(sourceItem.getDateResolved())), - есть задержка при парсинге контента. не проверяем
                    hasProperty("tags", equalTo(sourceItem.getTags()))
            )));
        }

    }

    @Test
    void getUserItems() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        user = userRepository.saveAndFlush(user);

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setTags(Set.of("Spring"));
        addItemRequest.setUrl("https://parseurlonline.com/");

        List<ItemDto> sourceItems = List.of(service.addNewItem(user.getId(), addItemRequest));
        List<ItemDto> targetItemDto = service.getUserItems("lastname");

        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItemDto, hasItem( allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("normalUrl", equalTo(sourceItem.getNormalUrl())),
                    hasProperty("resolvedUrl", equalTo(sourceItem.getResolvedUrl())),
                    hasProperty("mimeType", equalTo(sourceItem.getMimeType())),
                    hasProperty("title", equalTo(sourceItem.getTitle())),
                    hasProperty("hasImage", equalTo(sourceItem.isHasImage())),
                    hasProperty("hasVideo", equalTo(sourceItem.isHasVideo())),
                    hasProperty("unread", equalTo(sourceItem.isUnread())),
                    //hasProperty("dateResolved", equalTo(sourceItem.getDateResolved())), - есть задержка при парсинге контента. не проверяем
                    hasProperty("tags", equalTo(sourceItem.getTags()))
            )));
        }
    }

    @Test
    void changeItem() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email");
        user.setState(UserState.ACTIVE);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRegistrationDate(Instant.now());

        user = userRepository.saveAndFlush(user);

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setTags(Set.of("Spring"));
        addItemRequest.setUrl("https://parseurlonline.com/");

        List<ItemDto> sourceItems = List.of(service.addNewItem(user.getId(), addItemRequest));

        ModifyItemRequest modifyItemRequest = ModifyItemRequest.of(
            sourceItems.get(0).getId(), true, Set.of("Spring", "Boot"),true
        );

        List<ItemDto> targetItemDto = List.of(service.changeItem(user.getId(), modifyItemRequest));

        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItemDto, hasItem( allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("normalUrl", equalTo(sourceItem.getNormalUrl())),
                    hasProperty("resolvedUrl", equalTo(sourceItem.getResolvedUrl())),
                    hasProperty("mimeType", equalTo(sourceItem.getMimeType())),
                    hasProperty("title", equalTo(sourceItem.getTitle())),
                    hasProperty("hasImage", equalTo(sourceItem.isHasImage())),
                    hasProperty("hasVideo", equalTo(sourceItem.isHasVideo())),
                    hasProperty("unread", equalTo(!sourceItem.isUnread())),
                    //hasProperty("dateResolved", equalTo(sourceItem.getDateResolved())), - есть задержка при парсинге контента. не проверяем
                    hasProperty("tags", equalTo(Set.of("Spring", "Boot")))
            )));
        }
    }
}