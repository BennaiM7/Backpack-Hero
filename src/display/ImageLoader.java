package display;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

/**
 * The ImageLoader class deals with retrieving and storing images from files.
 * Uses a Map for flexible image management with string keys.
 */
public class ImageLoader {
	/**
	 * Map in which retrieved images are stored with their keys.
	 */
	private final Map<String, BufferedImage> images;

	/**
	 * Creates a new ImageLoader that will retrieve images from files.
	 * 
	 * @param dir Directory name where the files are located
	 * @param imageFiles File names where the images are located
	 */
	public ImageLoader(String dir, String... imageFiles) {
		Objects.requireNonNull(dir);
		Objects.requireNonNull(imageFiles);

		images = new HashMap<>();
		for (var imageFile : imageFiles) {
			loadImage(dir, imageFile);
		}
	}

	/**
	 * Retrieve a new image from a file, and stores it into the map.
	 * 
	 * @param dirPath Directory name
	 * @param imagePath File name
	 */
	private void loadImage(String dirPath, String imagePath) {
		Objects.requireNonNull(dirPath);
		Objects.requireNonNull(imagePath);
		var path = Path.of(dirPath + "/" + imagePath);
		try (var input = Files.newInputStream(path)) {
			var image = ImageIO.read(input);
			// Use filename without extension as key
			var key = imagePath.substring(0, imagePath.lastIndexOf('.'));
			images.put(key, image);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets an image by its key (filename without extension).
	 * 
	 * @param key Key of the image
	 * @return Image corresponding to the key
	 */
	public BufferedImage image(String key) {
		Objects.requireNonNull(key);
		return images.get(key);
	}

	/**
	 * Gets the number of images stored.
	 * 
	 * @return Number of images stored
	 */
	public int size() {
		return images.size();
	}
}
