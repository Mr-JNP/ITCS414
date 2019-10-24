import java.util.List;

/**
 * ***************** DO NOT MODIFY THIS FILE*************************
 * This class provide a placeholder object for a document.
 * @author Dr. Suppawong Tuarob (copyrighted, 2018)
 *
 */
public class Document implements Comparable
{
	private Integer id = -1;	//document/query ID
	private String rawText = null;	//raw text from the file
	private List<String> tokens = null;	//tokens after preprocessing raw text
	public int getId() {
		return id;
	}
	public Document(Integer id, String rawText, List<String> tokens) {
		this.id = id;
		this.rawText = rawText;
		this.tokens = tokens;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getRawText() {
		return rawText;
	}
	public void setRawText(String rawText) {
		this.rawText = rawText;
	}
	public List<String> getTokens() {
		return tokens;
	}
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}
	
	public String toString()
	{
		return "[ID:"+this.id+", "+(this.rawText.length() > 50? this.rawText.substring(0, 50)+"...":this.rawText)+"]";
	}
	
	@Override
	public int compareTo(Object arg0) {
		Document other = (Document)arg0;
		if(this.id == other.id && this.rawText != null && other.rawText != null) return this.rawText.compareTo(other.rawText);
		return this.id - other.id;
	}
	
}
