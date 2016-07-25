package de.espend.idea.shopware.external;

import de.espend.idea.php.toolbox.extension.JsonStreamResource;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class InternalJsonStreamResource implements JsonStreamResource {
    @NotNull
    @Override
    public Collection<InputStream> getInputStreams() {
        Collection<InputStream> inputStreams = new ArrayList<>();

        InputStream resourceAsStream = InternalJsonStreamResource.class.getClassLoader()
            .getResourceAsStream("resources/ide-toolbox.metadata.json");

        if(resourceAsStream == null) {
            return inputStreams;
        }

        inputStreams.add(resourceAsStream);

        return inputStreams;
    }
}
