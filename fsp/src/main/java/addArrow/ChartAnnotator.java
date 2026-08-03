package addArrow;

import java.io.IOException;

public interface ChartAnnotator {
    /**
     * Annotates a trading chart screenshot with an L-shaped arrow
     * pointing to the last candle.
     *
     * @param inputPath  path to the input PNG screenshot
     * @param outputPath path where the annotated PNG will be saved
     * @throws IOException if the file cannot be read or written
     */
    void annotate(String inputPath, String outputPath) throws IOException;
    void annotateLatestIn(String rootDirectoryPath) throws IOException;
}
