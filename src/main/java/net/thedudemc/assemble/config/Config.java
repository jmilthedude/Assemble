package net.thedudemc.assemble.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thedudemc.assemble.Assemble;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Random;

public abstract class Config {

    protected static final Random rand = new Random();
    private boolean isDirty = true;

    public void markDirty() {
        this.isDirty = true;
    }

    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();
    protected String root = "./config/";
    protected String extension = ".json";

    public void generateConfig() {
        this.reset();

        try {
            this.writeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConfigFile() {
        return new File(this.root + this.getName() + this.extension);
    }

    public abstract String getName();

    public Config readConfig() {
        try {
            return GSON.fromJson(new FileReader(this.getConfigFile()), (Type) this.getClass());
        } catch (FileNotFoundException e) {
            this.generateConfig();
        }

        return this;
    }

    protected abstract void reset();

    public void writeConfig() throws IOException {
        if (!this.isDirty) return;
        File dir = new File(this.root);
        if (!dir.exists() && !dir.mkdirs()) return;
        if (!this.getConfigFile().exists() && !this.getConfigFile().createNewFile()) return;
        FileWriter writer = new FileWriter(this.getConfigFile());
        GSON.toJson(this, writer);
        writer.flush();
        writer.close();
        this.isDirty = false;
    }

    protected void save() {
        try {
            this.writeConfig();
        } catch (Exception ex) {
            Assemble.getLogger().error("There was an error writing the config to file.");
            Assemble.getLogger().error(ex.getMessage());
        }
    }
}
