package nasirov.yv.data.animedia;

/**
 * Created by nasirov.yv
 */
public enum AnimeTypeOnAnimedia {
	MULTISEASONS("multi"), SINGLESEASON("single"), ANNOUNCEMENT("announcement"), ALL_TYPES("allTypes");

	private String description;

	AnimeTypeOnAnimedia(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
