package pm.lus.spaceship.routing.definition.endpoint.path.parts;

/**
 * Represents a literal path part
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class LiteralPart implements PathPart {

    private final String content;

    public LiteralPart(final String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public boolean matches(final PathPart other) {
        return other instanceof LiteralPart && ((LiteralPart) other).getContent().equals(this.content);
    }

    public boolean matchesCaseInsensitive(final PathPart other) {
        return other instanceof LiteralPart && ((LiteralPart) other).getContent().equalsIgnoreCase(this.content);
    }

}
