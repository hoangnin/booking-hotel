package com.lenin.hotel.repository.hotel;

    import com.lenin.hotel.common.enumuration.ImageType;
    import com.lenin.hotel.configuration.DatabaseTestContainer;
    import com.lenin.hotel.hotel.model.Image;
    import com.lenin.hotel.hotel.repository.ImageRepository;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
    import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.test.annotation.Rollback;
    import org.springframework.test.context.DynamicPropertyRegistry;
    import org.springframework.test.context.DynamicPropertySource;
    import org.testcontainers.containers.PostgreSQLContainer;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;

    @DataJpaTest
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @Rollback
    @Import(DatabaseTestContainer.class)
    public class ImageRepositoryTest {

        @Autowired
        private ImageRepository imageRepository;

        @Autowired
        private PostgreSQLContainer<?> postgreSQLContainer;

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", () -> DatabaseTestContainer.postgresqlContainer().getJdbcUrl());
            registry.add("spring.datasource.username", () -> DatabaseTestContainer.postgresqlContainer().getUsername());
            registry.add("spring.datasource.password", () -> DatabaseTestContainer.postgresqlContainer().getPassword());
        }

        @BeforeEach
        public void setUp() {
            // Clear existing data
            imageRepository.deleteAll();
            imageRepository.flush();

            // Create and persist Image 1
            Image image1 = Image.builder()
                    .url("http://example.com/image1.jpg")
                    .type(ImageType.HOTEL)
                    .referenceId(1)
                    .referenceTable("hotels")
                    .build();
            imageRepository.save(image1);

            // Create and persist Image 2
            Image image2 = Image.builder()
                    .url("http://example.com/image2.jpg")
                    .type(ImageType.ROOM)
                    .referenceId(1)
                    .referenceTable("rooms")
                    .build();
            imageRepository.save(image2);

            // Create and persist Image 3
            Image image3 = Image.builder()
                    .url("http://example.com/image3.jpg")
                    .type(ImageType.HOTEL)
                    .referenceId(2)
                    .referenceTable("hotels")
                    .build();
            imageRepository.save(image3);
        }

        @Test
        public void testFindByReferenceIdAndReferenceTableAndType() {
            // Act
            List<Image> images = imageRepository.findByReferenceIdAndReferenceTableAndType(1, "hotels", ImageType.HOTEL);

            // Assert
            assertThat(images).hasSize(1);
            assertThat(images.get(0).getUrl()).isEqualTo("http://example.com/image1.jpg");
            assertThat(images.get(0).getType()).isEqualTo(ImageType.HOTEL);
            assertThat(images.get(0).getReferenceId()).isEqualTo(1);
            assertThat(images.get(0).getReferenceTable()).isEqualTo("hotels");
        }

        @Test
        public void testFindByReferenceIdAndReferenceTableAndType_NoResults() {
            // Act
            List<Image> images = imageRepository.findByReferenceIdAndReferenceTableAndType(3, "hotels", ImageType.HOTEL);

            // Assert
            assertThat(images).isEmpty();
        }
    }