package com.thoughtworks.syngit.git;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
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
        gitDir = new File(workTreeDir.getAbsolutePath() + File.separator + ".git");
        committedFile = createFileWithContent(workTreeDir, "committedFile", "New file");
        execCommands(workTreeDir, "git add .", "git commit -am 'First commit'");
    }

    @Test
    public void shouldFindModifiedFiles() throws Exception {
        modify(committedFile);
        List<File> files = new GitFacade(gitDir).findChanges();
        assertThat(files.get(0).getAbsolutePath(), is(committedFile.getAbsolutePath()));
    }

    @Test
    public void shouldFindNewFiles() throws Exception {
        File newFileInRepository = createFileWithContent(workTreeDir, "newFile", "nothing important");
        List<File> files = new GitFacade(gitDir).findChanges();
        assertThat(files.get(0).getAbsolutePath(), is(newFileInRepository.getAbsolutePath()));
    }

    private void modify(File file) throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.append("\nModifying file at ").append(String.valueOf(new Date()));
        writer.flush();
        writer.close();
    }
}
