package com.efimchick.ifmo.io.filetree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FileTreeImpl implements FileTree {
    private final Set<Integer> fuckingTree = new HashSet<>();


    @Override
    public Optional<String> tree(Path path) {
        if (path == null || Files.notExists(path)) {
            return Optional.empty();
        } else {
            StringBuilder output = new StringBuilder();
            FileInfo fileInfo = wrapPath(path, 0);
            getTree(fileInfo, output);
            return Optional.of(output.toString());
        }
    }

    private void getTree(FileInfo fileInfo, StringBuilder output) {
        if (Files.isRegularFile(fileInfo.path())) {
            getFileOutputString(fileInfo, output);
        } else if (Files.isDirectory(fileInfo.path())) {
            getFileOutputString(fileInfo, output);
            List<Path> paths = getFilesListSorted(fileInfo.path());
            List<FileInfo> fileInfos = makeWarpedList(paths, fileInfo.getNesting() + 1);
            for (FileInfo file : fileInfos) {
                getTree(file, output);
            }
        }
    }

    private FileInfo wrapPath(Path path, int nesting) {
        return new FileInfo(path, nesting);
    }

    private List<FileInfo> makeWarpedList(List<Path> paths, int nesting) {
        List<FileInfo> fileInfos = new ArrayList<>();
        for (Path path : paths) {
            fileInfos.add(wrapPath(path, nesting));
        }
        markLastElement(fileInfos);
        return fileInfos;
    }

    private void markLastElement(List<FileInfo> fileInfos) {
        fileInfos.get(fileInfos.size() - 1).makeLastInList();
    }

    private List<Path> getFilesListSorted(Path path) {
        List<Path> files = new ArrayList<>();
        try {
            files = Files.list(path)
                    .sorted((p1, p2) -> p1.getFileName().toString()
                            .compareToIgnoreCase(p2.getFileName().toString()))
                    .sorted((p1, p2) -> {
                        int p1isDir = Files.isDirectory(p1) ? 1 : 0;
                        int p2isDir = Files.isDirectory(p2) ? 1 : 0;
                        return p2isDir - p1isDir;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    private void getFileOutputString(FileInfo path, StringBuilder output) {
        if (path.isNested()) {
            addIndentation(path, output);
        }
        try {
            long size = Files.isDirectory(path.path()) ?
                    getDirSize(path.path()) : Files.size(path.path());
            String bytes = "bytes";
            String stringSpace = " ";
            String newLine = "\n";
            output.append(path.path().getFileName())
                    .append(stringSpace)
                    .append(size)
                    .append(stringSpace)
                    .append(bytes)
                    .append(newLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addIndentation(FileInfo path, StringBuilder output) {
        String indentation = createIndentation(path);
        refreshListOfOpenedBranches(indentation);
        indentation = addOpenedBranches(indentation);
        output.append(indentation);
    }

    private String createIndentation(FileInfo fileInfo) {
        String twoSpaces = "   ";
        StringBuilder indentation = new StringBuilder()
                .append(twoSpaces.repeat(Math.max(0, fileInfo.nestingLevel - 1)));
        if (fileInfo.isLastInList()) {
            String lastFileOrDirectory = "└─ ";
            indentation.append(lastFileOrDirectory);
        } else {
            String fileOrDirectory = "├─ ";
            indentation.append(fileOrDirectory);
        }
        return indentation.toString();
    }

    private void refreshListOfOpenedBranches(String indentation) {
        char[] chars = indentation.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char forkWithoutDash = '├';
            char lastForkWithoutDash = '└';
            if (chars[i] == forkWithoutDash) {
                this.fuckingTree.add(i);
            } else if (chars[i] == lastForkWithoutDash) {
                this.fuckingTree.remove(i);
            }
        }
    }

    private String addOpenedBranches(String indentation) {
        char[] chars = indentation.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char charSpace = ' ';
            if (this.fuckingTree.contains(i)
                    && chars[i] == charSpace) {
                char stick = '│';
                chars[i] = stick;
            }
        }
        return String.valueOf(chars);
    }

    private long getDirSize(Path path) throws IOException {
        long size = 0;
        try { List<Path> files = Files.list(path).collect(Collectors.toList());
            for (Path file : files) {
                if (Files.isRegularFile(file)) {
                    size += Files.size(file);
                } else if (Files.isDirectory(file)) {
                    size += getDirSize(file);
                }
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return size;
    }
}
   /* StringBuilder result = new StringBuilder();
    private int numberOfInner = 0;
    static boolean isLastFolder = false;
    static boolean absolutelyLastFolder = false;

    @Override
    public Optional<String> tree(Path path)  {

        try {
            if(path == null || !path.toFile().exists())
                return Optional.empty();
            if (path.toFile().isFile())
                return Optional.of(path.toFile().getName() + " " + Files.size(path) + " bytes");
            else {
                //result.append(path.toFile().getName()).append(" ").append(getSizeOfDirectory(path)).append(" bytes\n");
                printDirectory(path, 0, result);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return Optional.ofNullable(result.toString());
    }


    private static long getSizeOfDirectory(Path path)
    { try {
        return Files.walk(path).
                filter(Files::isRegularFile).
                mapToLong(p -> {
                    try {
                        return Files.size(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }).sum();
    } catch (IOException ex) {
        ex.printStackTrace();}
        return 0;
    }

    private static void printDirectory(Path path, int inner, StringBuilder result)
    {
        ArrayList<File> directory = new ArrayList<>();
        ArrayList<File> file = new ArrayList<>();
        File[] files = path.toFile().listFiles();
        assert files != null;
        for (File value : files) {
            if (value.isDirectory())
                directory.add(value);
            else file.add(value);
        }

        directory.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        file.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        result.append(getString(inner));
        if(inner == 0)
            result.append(path.toFile().getName()).append(" ").append(getSizeOfDirectory(path)).append(" bytes\n");
        else if (isLastFolder)
        {
            result.append("└─ ").append(path.toFile().getName()).append(" ").append(getSizeOfDirectory(path)).append(" bytes\n");
        } else
             result.append("├─ ").append(path.toFile().getName()).append(" ").append(getSizeOfDirectory(path)).append(" bytes\n");
        for(int i = 0; i < directory.size(); ++i) {
            if(i == directory.size()-1  && file.size()==0) {
                isLastFolder = true;
                if(inner==1)
                    absolutelyLastFolder = true;
            }
            else {
                isLastFolder = false;
                absolutelyLastFolder = false;
            }
            printDirectory(directory.get(i).toPath(), inner+1, result);
        }
        boolean isLast = false;
        for(int i = 0; i < file.size(); ++i) {
            if(i == file.size()-1)
                isLast = true;
            printFile(file.get(i).toPath(), inner+1, result, isLast);
        }
    }
    private static String getString(int inner) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < inner - 1; ++i) {
                sb.append("│  ");
            }
             if(isLastFolder && sb.indexOf("│  ")!=-1 && inner != 2) {
                sb.replace(sb.lastIndexOf("│  "), sb.lastIndexOf("│  ") + 3, "   ");
            }
            return sb.toString();

    }
    private static void printFile(Path path, int inner, StringBuilder result, boolean last) {
        result.append(getString(inner));
        if(last)
            result.append("└─ ").append(path.toFile().getName()).append(" ").append(path.toFile().length()).append(" bytes\n");
        else
            result.append("├─ ").append(path.toFile().getName()).append(" ").append(path.toFile().length()).append(" bytes\n");
    }
*/


