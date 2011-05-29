package com.thoughtworks.syngit;

import com.thoughtworks.syngit.git.GitFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.io.File.createTempFile;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientRepositoryTest {

    private GitFacade git = mock(GitFacade.class);

    private File file1;

    private File file2;

    @Before
    public void setUp() throws Exception {
        file1 = createTempFile("file1", "");
        file2 = createTempFile("file2", "");
    }

    @After
    public void tearDown() throws Exception {
        file1.delete();
        file2.delete();
    }

    @Test
    public void shouldReturnAllFilesOnFirstCall() throws Exception {
        ClientRepository repository = new ClientRepository(git);
        when(git.findChanges()).thenReturn(files(file1, file2));

        List<File> changes = repository.findNewlyChangedFiles();

        assertThat(changes.size(), is(2));
        assertThat(changes.get(0), is(file1));
        assertThat(changes.get(1), is(file2));
    }

    @Test
    public void shouldNotReturnAnyFilesWhenChangesHaveAlreadyBeenProcessed() throws Exception {
        ClientRepository repository = new ClientRepository(git);
        when(git.findChanges()).thenReturn(files(file1, file2));

        List<File> changes = repository.findNewlyChangedFiles();
        assertThat(changes.size(), is(2));

        when(git.findChanges()).thenReturn(files(file1, file2));

        changes = repository.findNewlyChangedFiles();
        assertThat(changes.size(), is(0));
    }

    @Test
    public void shouldReturnOnlyNewlyChangedFilesWhenChangesHaveAlreadyBeenProcessed() throws Exception {
        ClientRepository repository = new ClientRepository(git);
        when(git.findChanges()).thenReturn(files(file1, file2));

        List<File> changes = repository.findNewlyChangedFiles();
        assertThat(changes.size(), is(2));

        file1.setLastModified(file1.lastModified() + 1000);
        when(git.findChanges()).thenReturn(files(file1, file2));

        changes = repository.findNewlyChangedFiles();
        assertThat(changes.size(), is(1));
        assertThat(changes.get(0), is(file1));
    }

    private List<File> files(File... files) {
        List<File> list = new ArrayList<File>();
        for (File file : files) {
            list.add(file);
        }
        return list;
    }
}
