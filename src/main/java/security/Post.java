package security;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Post {

	private String content;
	private String imageUrl;

	@Id
	@GeneratedValue
	private long id;

	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}

	public Post() {

	}
	
	public Post(String content, String imageUrl) {
		this.content = content;
		this.imageUrl = imageUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Post other = (Post) obj;
		if (id != other.id)
			return false;
		return true;
	}

}