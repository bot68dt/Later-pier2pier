package ru.practicum.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.NotFoundException;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.GetItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;
import ru.practicum.item.model.Item;
import ru.practicum.item.model.QItem;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Transactional(readOnly = false)
@ExtendWith(MockitoExtension.class)
class ItemServiceImplLogicTest {

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter
            .ofPattern("yyyy.MM.dd hh:mm:ss")
            .withZone(ZoneOffset.UTC);

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private static UserRepository userRepository;
    @Mock
    private static UrlMetaDataRetriever urlMetaDataRetriever;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItems_whenItemsExist_plusMapToItemDtoCheck() {
        Item item = new Item();
        item.setDateResolved(Instant.now());
        item.setTags(Set.of("Spring"));
        List<Item> userItemsExist = List.of(item);
        long userId = 0L;
        ItemDto itemDto = ItemDto.builder().id(userItemsExist.get(0).getId()).normalUrl(userItemsExist.get(0).getUrl())
                .resolvedUrl(userItemsExist.get(0).getResolvedUrl()).mimeType(userItemsExist.get(0).getMimeType())
                .title(userItemsExist.get(0).getTitle()).hasImage(userItemsExist.get(0).isHasImage())
                .hasVideo(userItemsExist.get(0).isHasVideo()).unread(userItemsExist.get(0).isUnread())
                .dateResolved(userItemsExist.get(0).getDateResolved()).tags(userItemsExist.get(0).getTags())
                .build();
        List<ItemDto> userItemDtosExist = new ArrayList<>();
        userItemDtosExist.add(itemDto);
        when(itemRepository.findByUserId(userId)).thenReturn(userItemsExist);

        List<Item> actualItemList = itemRepository.findByUserId(userId);
        List<ItemDto> actualItemDtoList = itemService.getItems(userId);

        assertEquals(actualItemList, userItemsExist);
        assertEquals(actualItemDtoList.getFirst().hashCode(), userItemDtosExist.getFirst().hashCode());
    }

    @Test
    void getItems_whenItemListIsEmpty() {
        List<Item> userItemsEmpty = new ArrayList<>();
        long userId = 0L;
        List<ItemDto> userItemDtosEmpty = ItemMapper.mapToItemDto(userItemsEmpty);
        when(itemRepository.findByUserId(userId)).thenReturn(userItemsEmpty);

        List<Item> actualItemList = itemRepository.findByUserId(userId);
        List<ItemDto> actualItemDtoList = itemService.getItems(userId);

        assertEquals(actualItemList, userItemsEmpty);
        assertEquals(actualItemDtoList, userItemDtosEmpty);
    }

    @Test
    void addNewItem_checkUserExists_andMbExistingItemIsPresent_andRequestHasTags() {
        User user = new User();
        long userId = 0L;
        AddItemRequest request =new AddItemRequest();
        request.setUrl("https://www.amrood.com/index.htm?language=en#j2se");
        request.setTags(Set.of("Spring"));
        UrlMetaDataRetrieverImpl.UrlMetadataImpl urlMetadata = new UrlMetaDataRetrieverImpl.UrlMetadataImpl("https://www.amrood.com/index.htm?language=en#j2se",
                "https://www.amrood.com/index.htm?language=en#j2se", "none",
                "none", false, false, Instant.now());
        Item item = new Item();
        item.setId(0L);
        item.setDateResolved(Instant.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(urlMetaDataRetriever.retrieve(request.getUrl())).thenReturn(urlMetadata);
        when(itemRepository.findByUserAndResolvedUrl(user, urlMetadata.getResolvedUrl()))
                .thenReturn(Optional.of(item));

        assertDoesNotThrow(() -> itemService.addNewItem(userId, request));
        assertEquals(itemService.addNewItem(userId, request).getTags(), Set.of("Spring"));
    }

    @Test
    void addNewItem_checkUserExists_andMbExistingItemIsEmpty() {
        User user = new User();
        long userId = 1L;
        AddItemRequest request =new AddItemRequest();
        request.setUrl("https://www.amrood.com/index.htm?language=en#j2se");
        request.setTags(Set.of("Spring"));
        UrlMetaDataRetrieverImpl.UrlMetadataImpl urlMetadata = new UrlMetaDataRetrieverImpl.UrlMetadataImpl("https://www.amrood.com/index.htm?language=en#j2se",
                "https://www.amrood.com/index.htm?language=en#j2se", "none",
                "none", false, false, Instant.now());
        Item item = new Item();
        item.setId(10L);
        item.setDateResolved(Instant.now());
        item.setTags(request.getTags());

        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        when(urlMetaDataRetriever.retrieve(request.getUrl())).thenReturn(urlMetadata);
        when(itemRepository.findByUserAndResolvedUrl(user, urlMetadata.getResolvedUrl()))
                .thenReturn(Optional.empty());
        lenient().when(itemRepository.save(Mockito.any()))
                .thenReturn(item);

        ItemDto newItem = itemService.addNewItem(userId, request);

        assertDoesNotThrow(() -> newItem);
        assertEquals(newItem.getTags(), Set.of("Spring"));
        verify(itemRepository, times(1))
                .save(Mockito.any());

    }

    @Test
    void testGetItems_whenStateAndContentTypeEqualsAll_andHasTags() {
        long userId = 1L;
        int limit = 1;
        List<String> tags = List.of("Spring");
        GetItemRequest req = GetItemRequest.of(userId, GetItemRequest.State.ALL.name(),
                GetItemRequest.ContentType.ALL.name(), "TITLE", limit, tags);
        Item item = new Item();
        item.setId(10L);
        item.setDateResolved(Instant.now());
        Iterable <Item> items = List.of(item);
        List<BooleanExpression> conditions = new ArrayList<>();
        QItem item1 = QItem.item;
        conditions.add(item1.user.id.eq(req.getUserId()));
        conditions.add(item1.tags.any().in(req.getTags()));
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();
        Sort sort = Sort.by("title").ascending();
        PageRequest pageRequest = PageRequest.of(0, req.getLimit(), sort);

        lenient().when(itemRepository.findAll(finalCondition, pageRequest)).thenReturn(items);

        itemService.getItems(req);

        verify(itemRepository, times(1))
                .findAll(finalCondition, pageRequest);

    }

    @Test
    void testGetItems_whenStateAndContentTypeNotEqualsAll_andHasNoTags() {
        long userId = 1L;
        int limit = 1;
        List<String> tags = new ArrayList<>();
        GetItemRequest req = GetItemRequest.of(userId, GetItemRequest.State.READ.name(),
                GetItemRequest.ContentType.ARTICLE.name(), "TITLE", limit, tags);
        Item item = new Item();
        item.setId(10L);
        item.setDateResolved(Instant.now());
        Iterable <Item> items = List.of(item);
        List<BooleanExpression> conditions = new ArrayList<>();
        QItem item1 = QItem.item;
        conditions.add(item1.user.id.eq(req.getUserId()));
        conditions.add(QItem.item.unread.isFalse());
        conditions.add(QItem.item.mimeType.eq("text"));
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();
        Sort sort = Sort.by("title").ascending();
        PageRequest pageRequest = PageRequest.of(0, req.getLimit(), sort);

        lenient().when(itemRepository.findAll(finalCondition, pageRequest)).thenReturn(items);

        itemService.getItems(req);

        verify(itemRepository, times(1))
                .findAll(finalCondition, pageRequest);

    }

    @Test
    void getUserItems_whenUserHasItems() {
        Item item = new Item();
        item.setId(10L);
        item.setDateResolved(Instant.now());
        List<Item> items = List.of(item);
        List<ItemDto> itemDtos = ItemMapper.mapToItemDto(items);

        when(itemRepository.findItemsByLastNamePrefix(anyString())).thenReturn(items);

        assertEquals(itemService.getUserItems("Spring"), itemDtos);
    }

    @Test
    void getUserItems_whenUserHasNoItems() {
        List<Item> items = new ArrayList<>();
        List<ItemDto> itemDtos = ItemMapper.mapToItemDto(items);

        when(itemRepository.findItemsByLastNamePrefix(anyString())).thenReturn(items);

        assertEquals(itemService.getUserItems("Spring"), itemDtos);
    }

    @Test
    void changeItem_whenItemExists_whenRequestHasTagsAndIsReplaced() {
        long userId = 1L;
        ModifyItemRequest request = ModifyItemRequest.of(1L, true, Set.of("Spring"), true);
        Item item = new Item();
        item.setId(10L);
        item.setDateResolved(Instant.now());
        User user = new User();
        user.setId(1L);
        item.setUser(user);
        Item item1 = item;
        item1.setUnread(!request.isRead());
        item1.getTags().clear();
        item1.getTags().addAll(request.getTags());
        ItemDto itemDto = ItemMapper.mapToItemDto(item1);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(item1)).thenReturn(item1);

        assertEquals(itemService.changeItem(userId, request), itemDto);
    }

    @Test
    void changeItem_whenItemExists_whenRequestHasNoTagsAndIsNotReplaced() {
        long userId = 1L;
        ModifyItemRequest request = ModifyItemRequest.of(1L, true, new HashSet<>(), false);
        Item item = new Item();
        item.setId(10L);
        item.setDateResolved(Instant.now());
        User user = new User();
        user.setId(1L);
        item.setUser(user);
        Item item1 = item;
        item1.setUnread(!request.isRead());
        ItemDto itemDto = ItemMapper.mapToItemDto(item1);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(item1)).thenReturn(item1);

        assertEquals(itemService.changeItem(userId, request), itemDto);
    }

    @Test
    void changeItem_whenItemDoesNotExist() {
        long userId = 1L;
        ModifyItemRequest request = ModifyItemRequest.of(1L, true, new HashSet<>(), false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.changeItem(userId, request));
    }
}