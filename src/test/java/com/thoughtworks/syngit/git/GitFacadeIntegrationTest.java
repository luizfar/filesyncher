        gitDir = new File(workTreeDir.getAbsolutePath(), ".git");
    public void shouldGetUntrackedFiles() throws Exception {
        File newFileInRepository = createFileWithContent(workTreeDir, "newFile", "nothing important");
        List<File> files = new GitFacade(gitDir).getUntrackedFiles();
        assertThat(files.get(0).getAbsolutePath(), is(newFileInRepository.getAbsolutePath()));
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
    private void modify(File file, String content) throws Exception {
        writer.append("\n").append(content);