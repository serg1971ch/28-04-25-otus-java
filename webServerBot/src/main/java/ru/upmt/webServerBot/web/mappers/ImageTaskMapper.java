package ru.upmt.webServerBot.web.mappers;

import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.exceptions.ImageProcessingException;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.web.dto.ImageTaskDto;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageTaskMapper implements Mappable<ImageTask, ImageTaskDto> {

        @Override
        public ImageTaskDto toDto(ImageTask entity) {
            if (entity == null) {
                return null;
            }
            ImageTaskDto dto = new ImageTaskDto();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setPathFile(entity.getPathFile());
            dto.setMediaType(entity.getMediaType());
            dto.setSize(entity.getSize());
            byte[] bytes = entity.getBytes();
            if (bytes != null && entity.getBytes().length > 0) {
                cropToSquare(bytes);
                bytes = scale(bytes, 300, 300);
                dto.setBase64Image(Base64.getEncoder().encodeToString(bytes));
            } else {
                dto.setBase64Image(null); // Или пустая строка, если данных нет
            }

            return dto;
        }

        // Метод для маппинга списка сущностей в список DTO
        public List<ImageTaskDto> toDto(List<ImageTask> entities) {
            if (entities == null) {
                return null;
            }
            return entities.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        @Override
        public ImageTask toEntity(ImageTaskDto dto) {
            if (dto == null) {
                return null;
            }
            ImageTask entity = new ImageTask();
            entity.setId(dto.getId());
            entity.setName(dto.getName());
            entity.setPathFile(dto.getPathFile());
            entity.setMediaType(dto.getMediaType());
            entity.setSize(dto.getSize());
            entity.setBytes(dto.getData()); // Если вы также передаете сырые байты

            // Если base64Image пришел, вы можете его декодировать здесь, чтобы сохранить в byte[] data сущности
            if (dto.getBase64Image() != null && !dto.getBase64Image().isEmpty()) {
                try {
                    entity.setBytes(Base64.getDecoder().decode(dto.getBase64Image()));
                } catch (IllegalArgumentException e) {
                    // Обработка ошибки декодирования Base64 при преобразовании в сущность
                    // log.error("Failed to decode Base64 string for ImageTaskDto (ID: {}): {}", dto.getId(), e.getMessage());
                }
            }
            return entity;
        }

        // Метод для маппинга списка DTO в список сущностей
        public List<ImageTask> toEntity(List<ImageTaskDto> dtos) {
            if (dtos == null) {
                return null;
            }
            return dtos.stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
        }

    public byte[] scale(byte[] fileData, int width, int height) {
        ByteArrayInputStream in = new ByteArrayInputStream(fileData);
        try {
            BufferedImage img = ImageIO.read(in);
            if(height == 0) {
                height = (width * img.getHeight())/ img.getWidth();
            }
            if(width == 0) {
                width = (height * img.getWidth())/ img.getHeight();
            }
            Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0,0,0), null);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ImageIO.write(imageBuff, "jpg", buffer);

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new ImageProcessingException("IOException in scale");
        }
    }

    public byte[] cropToSquare(byte[] imageBytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(imageBytes)) {
            BufferedImage originalImage = ImageIO.read(in);
            if (originalImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение из байтов");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int minSize = Math.min(width, height);

            // Координаты левого верхнего угла для обрезки
            int x = (width - minSize) / 2;
            int y = (height - minSize) / 2;

            // Вырезаем квадратный фрагмент
            BufferedImage croppedImage = originalImage.getSubimage(x, y, minSize, minSize);

            // Сохраняем в байты
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(croppedImage, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обрезке изображения в квадрат", e);
        }
    }
}
