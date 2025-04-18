package ru.yandex.practicum.item.note;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.item.Item;

import java.time.Instant;

@Data
@Entity
@Table(name = "item_notes", schema = "public")
public class ItemNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "timestamp")
    private Instant timeStamp = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemNote)) return false;
        return id != null && id.equals(((ItemNote) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
