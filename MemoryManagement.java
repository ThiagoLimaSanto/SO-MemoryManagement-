import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryManagement {

    private List<MemoryBlock> memory;
    private int lastPosition = 0;

    public MemoryManagement(int[] sizes) {
        memory = new ArrayList<>();
        for (int size : sizes) {
            memory.add(new MemoryBlock(size));
        }
    }

    // ---------- Algoritmos ----------
    // 1️⃣ First Fit
    public void firstFit(int processId, int size) {
        for (MemoryBlock block : memory) {
            if (!block.allocated && block.size >= size) {
                allocate(block, processId, size);
                return;
            }
        }
        System.out.println("❌ Não há espaço para o processo " + processId);
    }

    // 2️⃣ Next Fit
    public void nextFit(int processId, int size) {
        int count = 0;
        while (count < memory.size()) {
            MemoryBlock block = memory.get(lastPosition);
            if (!block.allocated && block.size >= size) {
                allocate(block, processId, size);
                lastPosition = (lastPosition + 1) % memory.size();
                return;
            }
            lastPosition = (lastPosition + 1) % memory.size();
            count++;
        }
        System.out.println("❌ Não há espaço para o processo " + processId);
    }

    // 3️⃣ Best Fit
    public void bestFit(int processId, int size) {
        MemoryBlock bestBlock = null;
        for (MemoryBlock block : memory) {
            if (!block.allocated && block.size >= size) {
                if (bestBlock == null || block.size < bestBlock.size) {
                    bestBlock = block;
                }
            }
        }
        if (bestBlock != null) {
            allocate(bestBlock, processId, size);
        } else {
            System.out.println("❌ Não há espaço para o processo " + processId);
        }
    }

    // 4️⃣ Quick Fit 
    public void quickFit(int processId, int size, Map<Integer, List<MemoryBlock>> quickLists) {
        int key = -1;
        for (int k : quickLists.keySet()) {
            if (k >= size) {
                key = k;
                break;
            }
        }
        if (key != -1) {
            for (MemoryBlock block : quickLists.get(key)) {
                if (!block.allocated && block.size >= size) {
                    allocate(block, processId, size);
                    return;
                }
            }
        }
        System.out.println("❌ Quick Fit não encontrou espaço para o processo " + processId);
    }

    // 5️⃣ Worst Fit
    public void worstFit(int processId, int size) {
        MemoryBlock worstBlock = null;
        for (MemoryBlock block : memory) {
            if (!block.allocated && block.size >= size) {
                if (worstBlock == null || block.size > worstBlock.size) {
                    worstBlock = block;
                }
            }
        }
        if (worstBlock != null) {
            allocate(worstBlock, processId, size);
        } else {
            System.out.println("❌ Não há espaço para o processo " + processId);
        }
    }

    // ---------- Métodos auxiliares ----------
    private void allocate(MemoryBlock block, int processId, int size) {
        block.allocated = true;
        block.processId = processId;
        System.out.println("✅ Processo " + processId + " alocado em bloco de " + block.size + " KB");
    }

    public void free(int processId) {
        for (MemoryBlock block : memory) {
            if (block.allocated && block.processId == processId) {
                block.allocated = false;
                block.processId = -1;
                System.out.println("🧹 Processo " + processId + " desalocado.");
                return;
            }
        }
        System.out.println("⚠ Processo " + processId + " não encontrado.");
    }

    public void showMemory() {
        System.out.println("\n📊 Estado atual da memória:");
        for (MemoryBlock block : memory) {
            System.out.print(block + " ");
        }
        System.out.println("\n");
    }


    public static void main(String[] args) {
        MemoryManagement mm = new MemoryManagement(new int[]{100, 500, 200, 300, 600});

        System.out.println("=== FIRST FIT ===");
        mm.firstFit(1, 212);
        mm.firstFit(2, 417);
        mm.firstFit(3, 112);
        mm.showMemory();

        mm.free(2);
        mm.showMemory();

        System.out.println("=== BEST FIT ===");
        mm.bestFit(4, 350);
        mm.showMemory();

        System.out.println("=== WORST FIT ===");
        mm.worstFit(5, 210);
        mm.showMemory();
    }
}
