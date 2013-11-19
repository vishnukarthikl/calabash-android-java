/**
 * 
 */
package calabash.java.android;

/**
 * 
 *
 */
public final class Rect {

	private final Integer x;
	private final Integer y;
	private final Integer width;
	private final Integer height;
	private final Integer center_x;
	private final Integer center_y;

	public Rect(Integer x, Integer y, Integer width, Integer height,
                Integer center_x, Integer center_y) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.center_x = center_x;
		this.center_y = center_y;
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public Integer getCenter_x() {
		return center_x;
	}

	public Integer getCenter_y() {
		return center_y;
	}

	public String toString() {
		return String
				.format("x: %s, y: %s, width: %s, height: %s, center_x: %s, center_y: %s",
						getX(), getY(), getWidth(), getHeight(), getCenter_x(),
						getCenter_y());
	}

	public boolean equals(Object obj) {
		if (obj instanceof Rect) {
			Rect that = (Rect) obj;
			boolean equal = false;

			if (this.getX() != null && that.getX() != null)
				equal = this.getX().equals(that.getX());

			if (equal && this.getY() != null && that.getY() != null)
				equal = this.getY().equals(that.getY());

			if (equal && this.getWidth() != null && that.getWidth() != null)
				equal = this.getWidth().equals(that.getWidth());

			if (equal && this.getHeight() != null && that.getHeight() != null)
				equal = this.getHeight().equals(that.getHeight());

			if (equal && this.getCenter_x() != null
					&& that.getCenter_x() != null)
				equal = this.getCenter_x().equals(that.getCenter_x());

			if (equal && this.getCenter_y() != null
					&& that.getCenter_y() != null)
				equal = this.getCenter_y().equals(that.getCenter_y());

			return equal;
		}

		return super.equals(obj);
	}
}
