package com.thoughtworks.syngit.git;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static com.thoughtworks.syngit.tests.Utils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GitFacadeIntegrationTest {

    private File gitDir;
    private File workTreeDir;
    private File committedFile;

    @Before
    public void setUp() throws Exception {
        workTreeDir = createTempGitRepository("temp-git");
        gitDir = new File(workTreeDir.getAbsolutePath(), ".git");
        committedFile = createFileWithContent(workTreeDir, "committedFile", "New file");
        execCommands(workTreeDir, "git add .", "git commit -am 'First commit'");
    }

    @Test
    public void shouldGetUntrackedFiles() throws Exception {
        File newFileInRepository = createFileWithContent(workTreeDir, "newFile", "nothing important");
        List<File> files = new GitFacade(gitDir).getUntrackedFiles();
        assertThat(files.get(0).getAbsolutePath(), is(newFileInRepository.getAbsolutePath()));
    }

    @Test
    public void shouldGetCachedDiff() throws Exception {
        File newFile = new File(workTreeDir, "a_new_file");
        modify(newFile, "a new file");
        execCommands(workTreeDir, "git add .");
        String cachedDiff = new GitFacade(gitDir).getCachedDiff();

        assertThat(cachedDiff.contains("new file mode"), is(true));
        assertThat(cachedDiff.contains("a new file"), is(true));
    }

    @Test
    public void shouldGetDiff() throws Exception {
        modify(committedFile, "Modifying file");
        String diff = new GitFacade(gitDir).getDiff();

        assertThat(diff.contains("-New file"), is(true));
        assertThat(diff.contains("+Modifying file"), is(true));
    }

    private void modify(File file, String content) throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.append("\n").append(content);
        writer.flush();
        writer.close();
    }
}
