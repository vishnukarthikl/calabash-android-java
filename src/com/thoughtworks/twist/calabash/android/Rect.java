/**
 * 
 */
package com.thoughtworks.twist.calabash.android;

/**
 * 
 *
 */
public final class Rect {

	private final Float x;
	private final Float y;
	private final Float width;
	private final Float height;
	private final Float center_x;
	private final Float center_y;

	public Rect(Float x, Float y, Float width, Float height,
                Float center_x, Float center_y) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.center_x = center_x;
		this.center_y = center_y;
	}

	public Float getX() {
		return x;
	}

	public Float getY() {
		return y;
	}

	public Float getWidth() {
		return width;
	}

	public Float getHeight() {
		return height;
	}

	public Float getCenter_x() {
		return center_x;
	}

	public Float getCenter_y() {
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

    @Override
    public int hashCode() {
        int result = x != null ? x.hashCode() : 0;
        result = 31 * result + (y != null ? y.hashCode() : 0);
        result = 31 * result + (width != null ? width.hashCode() : 0);
        result = 31 * result + (height != null ? height.hashCode() : 0);
        result = 31 * result + (center_x != null ? center_x.hashCode() : 0);
        result = 31 * result + (center_y != null ? center_y.hashCode() : 0);
        return result;
    }
}
