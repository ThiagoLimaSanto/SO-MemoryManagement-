import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BitMapMemoryManager {
    private static final int TAMANHO_MEMORIA = 32;
    private static final int TOTAL_ITERACOES = 30;

    static class Processo {
        int id;
        String nome;
        int tamanho;
        boolean alocado;
        int enderecoInicial; 

        public Processo(int id, int tamanho) {
            this.id = id;
            this.nome = "P" + id;
            this.tamanho = tamanho;
            this.alocado = false;
            this.enderecoInicial = -1;
        }

        public void reset() {
            this.alocado = false;
            this.enderecoInicial = -1;
        }
    }

    private static int[] memoria = new int[TAMANHO_MEMORIA];
    private static int ultimoIndiceNextFit = 0; 

    public static void main(String[] args) {
        // Definição dos Processos conforme diretivas
        List<Processo> gabaritoProcessos = new ArrayList<>();
        gabaritoProcessos.add(new Processo(1, 5));
        gabaritoProcessos.add(new Processo(2, 4));
        gabaritoProcessos.add(new Processo(3, 2));
        gabaritoProcessos.add(new Processo(4, 5));
        gabaritoProcessos.add(new Processo(5, 8));
        gabaritoProcessos.add(new Processo(6, 3));
        gabaritoProcessos.add(new Processo(7, 5));
        gabaritoProcessos.add(new Processo(8, 8));
        gabaritoProcessos.add(new Processo(9, 2));
        gabaritoProcessos.add(new Processo(10, 6));

        String[] algoritmos = {"First Fit", "Next Fit", "Best Fit", "Quick Fit", "Worst Fit"};

        for (String alg : algoritmos) {
            System.out.println("\n##################################################");
            System.out.println("INICIANDO SIMULAÇÃO: " + alg.toUpperCase());
            System.out.println("##################################################");
            
            // Prepara ambiente
            limparMemoria();
            ultimoIndiceNextFit = 0;
            // Clona processos para não afetar estado entre simulações
            List<Processo> processosSimulacao = new ArrayList<>();
            for (Processo p : gabaritoProcessos) {
                Processo novo = new Processo(p.id, p.tamanho);
                processosSimulacao.add(novo);
            }

            rodarSimulacao(alg, processosSimulacao);
        }
    }

    // --- Motor da Simulação ---
    private static void rodarSimulacao(String algoritmo, List<Processo> processos) {
        Random random = new Random();

        for (int i = 1; i <= TOTAL_ITERACOES; i++) {
            // Sorteia um processo
            Processo p = processos.get(random.nextInt(processos.size()));
            
            System.out.print("\n[" + i + "/" + TOTAL_ITERACOES + "] Processo Sorteado: " + p.nome + " (" + p.tamanho + " blocos). ");

            if (p.alocado) {
                // Se já está na memória, desaloque
                System.out.println("Status: JÁ NA MEMÓRIA -> Desalocando...");
                desalocar(p);
            } else {
                // Se não está, tente alocar
                System.out.println("Status: FORA DA MEMÓRIA -> Tentando Alocar (" + algoritmo + ")...");
                int indice = -1;

                switch (algoritmo) {
                    case "First Fit": indice = firstFit(p.tamanho); break;
                    case "Next Fit": indice = nextFit(p.tamanho); break;
                    case "Best Fit": indice = bestFit(p.tamanho); break;
                    case "Quick Fit": indice = quickFit(p.tamanho); break;
                    case "Worst Fit": indice = worstFit(p.tamanho); break;
                }

                if (indice != -1) {
                    alocarNoMapa(p, indice);
                    System.out.println(">> SUCESSO: Alocado no índice " + indice);
                } else {
                    System.out.println(">> FALHA: Sem espaço contíguo suficiente.");
                    // Calcula fragmentação externa simples (espaço livre total existe, mas não contíguo)
                    analisarFragmentacao(p.tamanho);
                }
            }
            imprimirMapa();
        }
    }

    // --- Operações no Mapa de Bits ---

    private static void limparMemoria() {
        Arrays.fill(memoria, 0);
    }

    private static void alocarNoMapa(Processo p, int indiceInicio) {
        for (int i = indiceInicio; i < indiceInicio + p.tamanho; i++) {
            memoria[i] = 1; // 1 = Ocupado
        }
        p.alocado = true;
        p.enderecoInicial = indiceInicio;
    }

    private static void desalocar(Processo p) {
        if (!p.alocado) return;
        
        for (int i = p.enderecoInicial; i < p.enderecoInicial + p.tamanho; i++) {
            memoria[i] = 0; // 0 = Livre
        }
        p.alocado = false;
        p.enderecoInicial = -1;
    }

    private static void imprimirMapa() {
        System.out.print("Mapa: " + Arrays.toString(memoria));
        // Visualização extra de uso
        int ocupados = 0;
        for (int b : memoria) if(b==1) ocupados++;
        System.out.println(" | Uso: " + ocupados + "/" + TAMANHO_MEMORIA);
    }

    private static void analisarFragmentacao(int tamanhoNecessario) {
        int totalLivres = 0;
        int maiorBuraco = 0;
        int buracoAtual = 0;

        for (int m : memoria) {
            if (m == 0) {
                totalLivres++;
                buracoAtual++;
            } else {
                if (buracoAtual > maiorBuraco) maiorBuraco = buracoAtual;
                buracoAtual = 0;
            }
        }
        if (buracoAtual > maiorBuraco) maiorBuraco = buracoAtual;

        if (totalLivres >= tamanhoNecessario) {
            System.out.println("   [!] Fragmentação Externa Detectada: Há " + totalLivres + 
                " blocos livres no total, mas o maior buraco é de " + maiorBuraco + " (Necessário: " + tamanhoNecessario + ")");
        } else {
            System.out.println("   [!] Memória Cheia: Não há memória física suficiente (" + totalLivres + " livres).");
        }
    }

    // --- Algoritmos de Alocação ---

    // 1. First Fit: Primeiro buraco que couber
    private static int firstFit(int tamanho) {
        for (int i = 0; i <= TAMANHO_MEMORIA - tamanho; i++) {
            if (isLivre(i, tamanho)) {
                return i;
            }
        }
        return -1;
    }

    // 2. Next Fit: Começa de onde parou. Circular.
    private static int nextFit(int tamanho) {
        int ponteiro = ultimoIndiceNextFit;
        int blocosVerificados = 0;

        // Percorre a memória circularmente até verificar todos os blocos possíveis
        while (blocosVerificados < TAMANHO_MEMORIA) {
            // Se o bloco atual + tamanho ultrapassa memória, volta pro início (wrap-around lógico para busca)
            if (ponteiro + tamanho > TAMANHO_MEMORIA) {
                ponteiro = 0; 
                blocosVerificados = TAMANHO_MEMORIA - ultimoIndiceNextFit; // Ajuste técnico do contador
                continue;
            }

            if (isLivre(ponteiro, tamanho)) {
                ultimoIndiceNextFit = ponteiro + tamanho; // Atualiza ponteiro global
                return ponteiro;
            }
            
            ponteiro++;
            blocosVerificados++;
            if (ponteiro >= TAMANHO_MEMORIA) ponteiro = 0;
        }
        return -1;
    }

    // 3. Best Fit: Menor buraco onde cabe o processo (menor sobra)
    private static int bestFit(int tamanho) {
        int melhorIndice = -1;
        int menorSobra = Integer.MAX_VALUE;

        // Vamos identificar todos os buracos (gaps)
        for (int i = 0; i < TAMANHO_MEMORIA; i++) {
            if (memoria[i] == 0) {
                // Encontrou início de um buraco. Calcula tamanho dele.
                int tamBuraco = 0;
                int j = i;
                while (j < TAMANHO_MEMORIA && memoria[j] == 0) {
                    tamBuraco++;
                    j++;
                }

                // Verifica se o processo cabe nesse buraco
                if (tamBuraco >= tamanho) {
                    int sobra = tamBuraco - tamanho;
                    if (sobra < menorSobra) {
                        menorSobra = sobra;
                        melhorIndice = i;
                    }
                }
                
                // Avança o loop principal para o fim do buraco
                i = j - 1; 
            }
        }
        return melhorIndice;
    }

    // 4. Quick Fit: Simulado para Bitmap.
    // Tenta achar um buraco de tamanho EXATO. Se não achar
    private static int quickFit(int tamanho) {
        // Passo 1: Busca encaixe exato (Tamanho do buraco == Tamanho do processo)
        for (int i = 0; i < TAMANHO_MEMORIA; i++) {
            if (memoria[i] == 0) {
                int tamBuraco = 0;
                int j = i;
                while (j < TAMANHO_MEMORIA && memoria[j] == 0) {
                    tamBuraco++;
                    j++;
                }
                
                if (tamBuraco == tamanho) {
                    return i; // Encaixe perfeito e rápido
                }
                i = j - 1;
            }
        }
        
        // Passo 2: Fallback para First Fit se não achar exato
        return firstFit(tamanho);
    }

    // 5. Worst Fit: Maior buraco disponível (maior sobra)
    private static int worstFit(int tamanho) {
        int piorIndice = -1;
        int maiorSobra = -1;

        for (int i = 0; i < TAMANHO_MEMORIA; i++) {
            if (memoria[i] == 0) {
                // Mede o buraco
                int tamBuraco = 0;
                int j = i;
                while (j < TAMANHO_MEMORIA && memoria[j] == 0) {
                    tamBuraco++;
                    j++;
                }

                if (tamBuraco >= tamanho) {
                    int sobra = tamBuraco - tamanho;
                    if (sobra > maiorSobra) {
                        maiorSobra = sobra;
                        piorIndice = i;
                    }
                }
                i = j - 1;
            }
        }
        return piorIndice;
    }

    // --- Helper: Verifica se um segmento é livre ---
    private static boolean isLivre(int inicio, int tamanho) {
        // Verifica limites
        if (inicio + tamanho > TAMANHO_MEMORIA) return false;

        // Verifica bits
        for (int i = inicio; i < inicio + tamanho; i++) {
            if (memoria[i] == 1) return false;
        }
        return true;
    }
}