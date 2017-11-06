package other;

import org.apache.commons.collections15.Factory;

public class MyLink {
	private double weight;
	private final int id;
	private static int serialID = 0;

	public MyLink(double weight) {
		this.weight = weight;
		this.id = serialID++;

	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public static Factory<MyLink> getFactory() {
		Factory<MyLink> edgeFactory = new Factory<MyLink>() {
			public MyLink create() {
				return new MyLink(1);
			}
		};
		return edgeFactory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		MyLink other = (MyLink) obj;
		if (id != other.id)
			return false;
		return true;
	}
}