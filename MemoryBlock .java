class MemoryBlock {
    int size;
    boolean allocated;
    int processId;

    public MemoryBlock(int size) {
        this.size = size;
        this.allocated = false;
        this.processId = -1;
    }

    @Override
    public String toString() {
        return allocated
                ? "[Processo " + processId + " | " + size + " KB]"
                : "[Livre | " + size + " KB]";
    }
}