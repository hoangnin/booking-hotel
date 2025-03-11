package com.lenin.hotel.hotel.model;

import com.lenin.hotel.common.enumuration.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //@Lob (Large Object) giúp lưu dữ liệu lớn hơn 255 ký tự.
    @Lob
    @Column(columnDefinition = "TEXT")
    private String url;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType type;

    // không sử dụng relation (manytomany,..) vì tránh dư thừa field
    private Integer referenceId;
    @Column(nullable = false)
    private String referenceTable;

//    @ManyToOne
//    @JoinColumn(name = "review_id", nullable = false)
//    private Review review;

    @Column(name = "create_dt")
    @CreationTimestamp
    private ZonedDateTime createDt;

    @Column(name = "update_dt")
    @UpdateTimestamp
    private ZonedDateTime updateDt;

}
