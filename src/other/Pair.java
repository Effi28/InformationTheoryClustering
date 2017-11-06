package other;
public class Pair implements Comparable<Pair> {
	int x;
	int y;
	double value;

	public Pair(int x, int y, double d) {
		this.x = x;
		this.y = y;
		this.value = d;
	}

	@Override
	public int compareTo(Pair o) {
		if (this.value < o.value) {
			return -1;
		} else if(this.value == o.value) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + x + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		if (x != other.y && y != other.x)
			return false;
		return true;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public double getValue() {
		return value;
	}
}
