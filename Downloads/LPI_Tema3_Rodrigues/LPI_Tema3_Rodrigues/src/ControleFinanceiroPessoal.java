/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  *
- Disciplina : Linguagem de Programacao I                                                                       *
- Prof . Verissimo                                                                                              *
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
- Objetivo do Programa : Controle financeiro pessoal completo - final (Etapa 5 - Sequencia 5).                  *
- Data - 27/11/2026                                                                                             *
- Autor : Joyce Rodrigues Teotonio                                                                              *
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class controleFinanceiroSEQ5 {

    static final int MAX_LANCAMENTOS = 300;
    static final String[] NOMES_MESES = {
            "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };
    static final int[] DIAS_POR_MES = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    static final String[] CATEGORIAS_RECEITAS = { "Salario", "Renda extra", "Outras receitas" };
    static final String[] CATEGORIAS_DESPESAS = {
            "Moradia", "Alimentacao", "Transporte", "Saude", "Educacao", "Lazer", "Outras despesas"
    };

    static int[] codigos = new int[MAX_LANCAMENTOS];
    static String[] descricoes = new String[MAX_LANCAMENTOS];
    static int[] tipos = new int[MAX_LANCAMENTOS];
    static int[] categorias = new int[MAX_LANCAMENTOS];
    static double[] valores = new double[MAX_LANCAMENTOS];
    static int[] meses = new int[MAX_LANCAMENTOS];
    static int[] dias = new int[MAX_LANCAMENTOS];
    static boolean[] ativos = new boolean[MAX_LANCAMENTOS];
    static int qtLancamentos = 0;
    static int proximoCodigo = 1;

    static double[] orcamentos = new double[7];
    static double[] receitasPorMes = new double[12];
    static double[][] despesasCategoriasMes = new double[7][12];
    static Scanner sc = new Scanner(System.in);

    static int lerInteiro(String msg, int min, int max) {
        int numero = 0;
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            String linha = sc.nextLine();
            linha = linha.trim();
            try {
                numero = Integer.parseInt(linha);
                if (numero >= min && numero <= max) {
                    ok = true;
                } else {
                    System.out.println("Erro: digite entre " + min + " e " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite apenas numeros inteiros.");
            }
        }
        return numero;
    }

    static double lerDecimal(String msg, double min, double max, boolean podeZero) {
        double valor = 0;
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            String texto = sc.nextLine();
            texto = texto.trim();
            texto = texto.replace(",", ".");
            try {
                valor = Double.parseDouble(texto);
                if (podeZero) {
                    if (valor >= min && valor <= max) {
                        ok = true;
                    } else {
                        System.out.println("Erro: o valor tem que ficar entre " + min + " e " + max + ".");
                    }
                } else {
                    if (valor > min && valor <= max) {
                        ok = true;
                    } else {
                        System.out.println("Erro: o valor deve ser maior que " + min + " e ate " + max + ".");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite um valor numerico valido.");
            }
        }
        return valor;
    }

    static String lerTexto(String msg, boolean semPontoVirgula) {
        String texto = "";
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            texto = sc.nextLine();
            texto = texto.trim();
            if (texto.isEmpty()) {
                System.out.println("Erro: a descricao nao pode ficar vazia.");
            } else if (semPontoVirgula && texto.contains(";")) {
                System.out.println("Erro: nao use ponto e virgula (;).");
            } else {
                ok = true;
            }
        }
        return texto;
    }

    static boolean pedeSimNao(String msg) {
        boolean resultado = false;
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            String resp = sc.nextLine();
            resp = resp.trim();
            resp = resp.toUpperCase();
            if (resp.equals("S")) {
                resultado = true;
                ok = true;
            } else if (resp.equals("N")) {
                resultado = false;
                ok = true;
            } else {
                System.out.println("Erro: digite S ou N.");
            }
        }
        return resultado;
    }

    // carrega os dados ao abrir (RF15), mostra o menu e salva tudo ao sair (RF16)
    public static void main(String[] args) {
        carregarDados();
        int opcao = -1;
        do {
            System.out.println("\n=== MINHAS FINANCAS - CONTROLE FINANCEIRO PESSOAL ===");
            System.out.println("1 - Registrar receita");
            System.out.println("2 - Registrar despesas");
            System.out.println("3 - Alterar lancamento");
            System.out.println("4 - Cancelar lancamento");
            System.out.println("5 - Listar lancamentos");
            System.out.println("6 - Consultar lancamento por codigo");
            System.out.println("7 - Pesquisar despesas por categoria");
            System.out.println("8 - Extrato mensal");
            System.out.println("9 - Definir orcamento mensal por categoria");
            System.out.println("10 - Resumo financeiro anual");
            System.out.println("11 - Despesas por categoria e mes");
            System.out.println("12 - Orcamento previsto x realizado");
            System.out.println("13 - Indicadores financeiros");
            System.out.println("14 - Exportar relatorio anual");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opcao: ");

            try {
                String entrada = sc.nextLine().trim();
                opcao = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas numeros.");
                continue;
            }

            if (opcao == 0) {
                salvarDados();
                System.out.println("Adeus");
            } else if (opcao == 1) {
                registrarReceita();
            } else if (opcao == 2) {
                registrarDespesa();
            } else if (opcao == 3) {
                alterarLancamento();
            } else if (opcao == 4) {
                cancelarLancamento();
            } else if (opcao == 5) {
                listarLancamentos();
            } else if (opcao == 6) {
                consultarPorCodigo();
            } else if (opcao == 7) {
                pesquisarDespesasCategoria();
            } else if (opcao == 8) {
                extratoMensal();
            } else if (opcao == 9) {
                definirOrcamento();
            } else if (opcao == 10) {
                relatorioAnual();
            } else if (opcao == 11) {
                relatorioSemestral();
            } else if (opcao == 12) {
                orcamentoPrevistoRealizado();
            } else if (opcao == 13) {
                indicadoresFinanceiros();
            } else if (opcao == 14) {
                exportarResumoAnual();
            } else {
                System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
        sc.close();
    }

    // RF01 - Registrar receita (aplica RN01 a RN06)
    static void registrarReceita() {
        System.out.println("\n== REGISTRAR RECEITA ==");
        // RN06: verifica limite de capacidade do vetor
        if (qtLancamentos >= MAX_LANCAMENTOS) {
            System.out.println("Erro: limite maximo de lancamentos atingido.");
            return;
        }

        // RN01, RN02, RN03, RN04: leitura e validacao dos campos
        String descricao = lerTexto("Descricao: ", true);
        System.out.println("Categorias: 1- Salario | 2- Renda extra | 3- Outras receitas");
        int categoria = lerInteiro("Categoria (1 a 3): ", 1, 3);
        double valor = lerDecimal("Valor (R$): ", 0, 1000000.0, false);
        int mes = lerInteiro("Mes (1 a 12): ", 1, 12);
        int maxDias = DIAS_POR_MES[mes - 1];
        int dia = lerInteiro("Dia (1 a " + maxDias + "): ", 1, maxDias);

        // RN05: codigo sequencial unico e status ativo = true
        int i = qtLancamentos;
        codigos[i] = proximoCodigo;
        descricoes[i] = descricao;
        tipos[i] = 1;
        categorias[i] = categoria;
        valores[i] = valor;
        meses[i] = mes;
        dias[i] = dia;
        ativos[i] = true;

        int idxMes = mes - 1;
        receitasPorMes[idxMes] = receitasPorMes[idxMes] + valor;

        qtLancamentos = qtLancamentos + 1;
        proximoCodigo = proximoCodigo + 1;

        boolean salvou = salvarLancamentos();
        if (salvou) {
            System.out.println("Receita registrada com sucesso! Codigo: " + codigos[i]);
        }
    }

    // RF02 - Registrar despesa (aplica RN01 a RN06, RN10 e RN11)
    static void registrarDespesa() {
        System.out.println("\n== REGISTRAR DESPESA ==");
        // RN06: verifica limite de capacidade do vetor
        if (qtLancamentos >= MAX_LANCAMENTOS) {
            System.out.println("Erro: limite maximo de lancamentos atingido.");
            return;
        }

        // RN01, RN02, RN03, RN04: leitura e validacao dos campos
        String descricao = lerTexto("Descricao: ", true);
        System.out.println(
                "Categorias: 1- Moradia | 2- Alimentacao | 3- Transporte | 4- Saude | 5- Educacao | 6- Lazer | 7- Outras despesas");
        int categoria = lerInteiro("Categoria (1 a 7): ", 1, 7);
        double valor = lerDecimal("Valor (R$): ", 0, 1000000.0, false);
        int mes = lerInteiro("Mes (1 a 12): ", 1, 12);
        int maxDias = DIAS_POR_MES[mes - 1];
        int dia = lerInteiro("Dia (1 a " + maxDias + "): ", 1, maxDias);

        // RN05: codigo sequencial unico e status ativo = true
        int i = qtLancamentos;
        codigos[i] = proximoCodigo;
        descricoes[i] = descricao;
        tipos[i] = 2;
        categorias[i] = categoria;
        valores[i] = valor;
        meses[i] = mes;
        dias[i] = dia;
        ativos[i] = true;

        int idxCat = categoria - 1;
        int idxMes = mes - 1;
        despesasCategoriasMes[idxCat][idxMes] = despesasCategoriasMes[idxCat][idxMes] + valor;

        qtLancamentos = qtLancamentos + 1;
        proximoCodigo = proximoCodigo + 1;

        boolean salvou = salvarLancamentos();
        if (salvou) {
            System.out.println("Despesa registrada com sucesso! Codigo: " + codigos[i]);
        }

        // RN10: avisa se passou do limite do orcamento da categoria e mostra quanto
        // excedeu
        double limite = orcamentos[idxCat];
        double gastoCategoriaMes = despesasCategoriasMes[idxCat][idxMes];
        if (limite > 0 && gastoCategoriaMes > limite) {
            double excesso = gastoCategoriaMes - limite;
            System.out.println("ALERTA: Orcamento de " + CATEGORIAS_DESPESAS[idxCat] + " excedido em "
                    + NOMES_MESES[idxMes] + "! Orcamento: R$ " + String.format("%.2f", limite)
                    + " | Gasto no mes: R$ " + String.format("%.2f", gastoCategoriaMes)
                    + " | Passou: R$ " + String.format("%.2f", excesso));
        }

        // RN11: avisa se o saldo do mes ficou negativo
        double totalDespesasMes = 0;
        for (int c = 0; c < 7; c++) {
            totalDespesasMes = totalDespesasMes + despesasCategoriasMes[c][idxMes];
        }
        double saldoMes = receitasPorMes[idxMes] - totalDespesasMes;
        if (saldoMes < 0) {
            System.out.println("ALERTA: saldo de " + NOMES_MESES[idxMes] + " ficou NEGATIVO: R$ "
                    + String.format("%.2f", saldoMes));
        }
    }

    // RF03 - Alterar lancamento (aplica RN01, RN02, RN07 e RN12)
    static void alterarLancamento() {
        System.out.println("\n== ALTERAR LANCAMENTO ==");
        int codigoBuscar = lerInteiro("Digite o codigo do lancamento: ", 1, 999999);
        int idx = -1;

        for (int i = 0; i < qtLancamentos; i++) {
            if (codigos[i] == codigoBuscar && ativos[i]) {
                idx = i;
                break;
            }
        }

        // RN07: so permite alterar lancamento existente e ativo
        if (idx == -1) {
            System.out.println("Erro: lancamento nao encontrado ou ja cancelado.");
            return;
        }

        String descAntiga = descricoes[idx];
        double valorAntigo = valores[idx];
        int idxCat = categorias[idx] - 1;
        int idxMes = meses[idx] - 1;

        System.out.println("Encontrado: " + descAntiga + " | R$ " + String.format("%.2f", valorAntigo)
                + " | Data: " + dias[idx] + "/" + meses[idx]);

        // RN12: estorna o valor antigo dos totalizadores antes de somar o novo
        if (tipos[idx] == 1) {
            receitasPorMes[idxMes] = receitasPorMes[idxMes] - valorAntigo;
        } else {
            despesasCategoriasMes[idxCat][idxMes] = despesasCategoriasMes[idxCat][idxMes] - valorAntigo;
        }

        // RN07: altera apenas descricao e valor
        String novaDesc = lerTexto("Nova descricao: ", true);
        double novoValor = lerDecimal("Novo valor (R$): ", 0, 1000000.0, false);

        descricoes[idx] = novaDesc;
        valores[idx] = novoValor;

        // RN12: atualiza os totalizadores com o novo valor
        if (tipos[idx] == 1) {
            receitasPorMes[idxMes] = receitasPorMes[idxMes] + novoValor;
        } else {
            despesasCategoriasMes[idxCat][idxMes] = despesasCategoriasMes[idxCat][idxMes] + novoValor;
        }

        boolean salvou = salvarLancamentos();
        if (salvou) {
            System.out.println("Lancamento alterado com sucesso!");
            System.out.println("Valor anterior: R$ " + String.format("%.2f", valorAntigo)
                    + " | Novo valor: R$ " + String.format("%.2f", novoValor));
            System.out.println("Descricao anterior: " + descAntiga + " | Nova: " + novaDesc);
        }
    }

    // RF04 - Cancelar lancamento (exclusao logica, aplica RN08 e RN12)
    static void cancelarLancamento() {
        System.out.println("\n== CANCELAR LANCAMENTO ==");
        int codigoBusca = lerInteiro("Digite o codigo de lancamento para cancelar: ", 1, 999999);
        int idx = -1;

        for (int i = 0; i < qtLancamentos; i++) {
            if (codigos[i] == codigoBusca && ativos[i]) {
                idx = i;
                break;
            }
        }

        // RN08: bloqueia cancelamento se nao existe ou se ja esta cancelado
        if (idx == -1) {
            System.out.println("Erro: lancamento nao encontrado ou ja cancelado.");
            return;
        }

        System.out.println("Lancamento: " + descricoes[idx] + " | R$ " + String.format("%.2f", valores[idx]));
        boolean querCancelar = pedeSimNao("Confirmar cancelamento? (S/N): ");

        if (querCancelar) {
            // RN08: exclusao logica mantendo o registro e o codigo reservado
            ativos[idx] = false;
            int idxCat = categorias[idx] - 1;
            int idxMes = meses[idx] - 1;

            // RN12: desconta o valor cancelado do total mensal ou da matriz
            if (tipos[idx] == 1) {
                receitasPorMes[idxMes] = receitasPorMes[idxMes] - valores[idx];
            } else {
                despesasCategoriasMes[idxCat][idxMes] = despesasCategoriasMes[idxCat][idxMes] - valores[idx];
            }

            boolean salvou = salvarLancamentos();
            if (salvou) {
                System.out.println("Lancamento cancelado com sucesso!");
            }
        } else {
            System.out.println("Cancelamento abortado.");
        }
    }

    // RF05 - Listar lancamentos ativos (aplica RN12)
    static void listarLancamentos() {
        System.out.println("\n== LISTA DE LANCAMENTOS ==");
        double totalReceitas = 0;
        double totalDespesas = 0;
        int cont = 0;

        for (int i = 0; i < qtLancamentos; i++) {
            // RN12: considera somente registros ativos
            if (ativos[i]) {
                String tipoTexto;
                String nomeCat;
                int idxCat = categorias[i] - 1;

                if (tipos[i] == 1) {
                    tipoTexto = "Receita";
                    nomeCat = CATEGORIAS_RECEITAS[idxCat];
                    totalReceitas = totalReceitas + valores[i];
                } else {
                    tipoTexto = "Despesa";
                    nomeCat = CATEGORIAS_DESPESAS[idxCat];
                    totalDespesas = totalDespesas + valores[i];
                }

                System.out.println(codigos[i] + " | " + dias[i] + "/" + meses[i] + " | " + tipoTexto + " | " + nomeCat
                        + " | " + descricoes[i] + " | R$ " + String.format("%.2f", valores[i]));
                cont = cont + 1;
            }
        }

        if (cont == 0) {
            System.out.println("Nenhum lancamento cadastrado.");
        } else {
            double saldo = totalReceitas - totalDespesas;
            System.out.println("\nTOTAL receitas: R$ " + String.format("%.2f", totalReceitas));
            System.out.println("Total despesas: R$ " + String.format("%.2f", totalDespesas));
            System.out.println("Saldo: R$ " + String.format("%.2f", saldo));
        }
    }

    // RF06 - Consultar lancamento por codigo (mostra ativos e cancelados)
    static void consultarPorCodigo() {
        System.out.println("\n== CONSULTAR POR CODIGO ==");
        int codBusca = lerInteiro("Digite o codigo: ", 1, 999999);

        for (int i = 0; i < qtLancamentos; i++) {
            if (codigos[i] == codBusca) {
                String tipoTexto;
                String nomeCat;
                int idxCat = categorias[i] - 1;

                if (tipos[i] == 1) {
                    tipoTexto = "Receita";
                    nomeCat = CATEGORIAS_RECEITAS[idxCat];
                } else {
                    tipoTexto = "Despesa";
                    nomeCat = CATEGORIAS_DESPESAS[idxCat];
                }

                String sit = "Cancelado";
                if (ativos[i]) {
                    sit = "Ativo";
                }

                System.out.println("Codigo: " + codigos[i] + " | " + tipoTexto + " | " + nomeCat);
                System.out.println("Descricao: " + descricoes[i] + " | Valor: R$ " + String.format("%.2f", valores[i]));
                System.out.println("Data: " + dias[i] + "/" + meses[i] + " | Status: " + sit);
                return;
            }
        }

        System.out.println("Lancamento nao encontrado.");
    }

    // RF07 - Pesquisar despesas por categoria (aplica RN12)
    static void pesquisarDespesasCategoria() {
        System.out.println("\n== PESQUISAR DESPESAS POR CATEGORIA ==");
        System.out.println(
                "1- moradia | 2- alimentacao | 3- transporte | 4- saude | 5- educacao | 6- lazer | 7- outras despesas");
        int catEscolhida = lerInteiro("Categoria (1 a 7): ", 1, 7);

        double soma = 0;
        int achados = 0;

        for (int i = 0; i < qtLancamentos; i++) {
            boolean ehAtivo = ativos[i];
            boolean ehDespesa = (tipos[i] == 2);
            boolean mesmaCategoria = (categorias[i] == catEscolhida);

            if (ehAtivo && ehDespesa && mesmaCategoria) {
                System.out.println(codigos[i] + " | " + dias[i] + "/" + meses[i] + " | " + descricoes[i]
                        + " | R$ " + String.format("%.2f", valores[i]));
                soma = soma + valores[i];
                achados = achados + 1;
            }
        }

        if (achados == 0) {
            System.out.println("Nenhuma despesa nessa categoria.");
        } else {
            double media = soma / achados;
            System.out.println("Total: R$ " + String.format("%.2f", soma));
            System.out.println("Media: R$ " + String.format("%.2f", media));
        }
    }

    // RF08 - Emitir extrato mensal (aplica RN11 e RN12)
    static void extratoMensal() {
        System.out.println("\n== EXTRATO MENSAL ==");
        int mes = lerInteiro("Digite o mes (1 a 12): ", 1, 12);
        int idxMes = mes - 1;
        String nomeMesMaiusculo = NOMES_MESES[idxMes].toUpperCase();
        System.out.println("\nEXTRATO DE " + nomeMesMaiusculo);

        double recMes = 0;
        double despMes = 0;
        int qt = 0;

        for (int i = 0; i < qtLancamentos; i++) {
            if (ativos[i] && meses[i] == mes) {
                String tipoTexto;
                String nomeCat;
                int idxCat = categorias[i] - 1;

                if (tipos[i] == 1) {
                    tipoTexto = "Receita";
                    nomeCat = CATEGORIAS_RECEITAS[idxCat];
                    recMes = recMes + valores[i];
                } else {
                    tipoTexto = "Despesa";
                    nomeCat = CATEGORIAS_DESPESAS[idxCat];
                    despMes = despMes + valores[i];
                }

                System.out.println(codigos[i] + " | Dia " + dias[i] + " | " + tipoTexto + " | " + nomeCat
                        + " | " + descricoes[i] + " | R$ " + String.format("%.2f", valores[i]));
                qt = qt + 1;
            }
        }

        if (qt == 0) {
            System.out.println("Nenhum lancamento neste mes.");
            return;
        }

        // RN11: indica se o saldo do mes ficou positivo ou negativo
        double saldo = recMes - despMes;
        String situacao = "POSITIVO";
        if (saldo < 0) {
            situacao = "NEGATIVO";
        }

        System.out.println("Receitas: R$ " + String.format("%.2f", recMes));
        System.out.println("Despesas: R$ " + String.format("%.2f", despMes));
        System.out.println("Saldo: R$ " + String.format("%.2f", saldo) + " (" + situacao + ")");
    }

    // RF09 - Definir orcamento mensal por categoria (aplica RN09)
    static void definirOrcamento() {
        System.out.println("\n== DEFINIR ORCAMENTO POR CATEGORIA ==");
        for (int c = 0; c < 7; c++) {
            System.out.println((c + 1) + " - " + CATEGORIAS_DESPESAS[c]);
        }

        // RN09: valor zero remove o limite da categoria; valor maior que zero define o
        // limite
        int categoria = lerInteiro("Escolha a categoria (1 a 7): ", 1, 7);
        double limite = lerDecimal("Limite mensal em R$ (0 para remover limite): ", 0, 1000000.0, true);

        int idxCat = categoria - 1;
        orcamentos[idxCat] = limite;

        boolean salvou = salvarOrcamentos();
        if (salvou) {
            System.out.println("Orcamento de " + CATEGORIAS_DESPESAS[idxCat] + " definido para R$ "
                    + String.format("%.2f", limite));
            System.out.println("\nOrcamentos vigentes:");
            for (int c = 0; c < 7; c++) {
                if (orcamentos[c] > 0) {
                    System.out.println(" " + CATEGORIAS_DESPESAS[c] + ": R$ " + String.format("%.2f", orcamentos[c]));
                } else {
                    System.out.println(" " + CATEGORIAS_DESPESAS[c] + ": SEM LIMITE");
                }
            }
        }
    }

    // RF10 - Resumo financeiro anual (aplica RN11 e RN12)
    static void relatorioAnual() {
        System.out.println("\n== RELATORIO ANUAL ==");
        double totalReceitasAno = 0;
        double totalDespesasAno = 0;

        System.out.println("== MES A MES ==");
        for (int m = 0; m < 12; m++) {
            double despMes = 0;
            for (int c = 0; c < 7; c++) {
                despMes = despMes + despesasCategoriasMes[c][m];
            }

            double recMes = receitasPorMes[m];
            double saldoMes = recMes - despMes;

            totalReceitasAno = totalReceitasAno + recMes;
            totalDespesasAno = totalDespesasAno + despMes;

            System.out.println(NOMES_MESES[m] + " > Receitas: R$ " + String.format("%.2f", recMes)
                    + " | Despesas: R$ " + String.format("%.2f", despMes)
                    + " | Saldo: R$ " + String.format("%.2f", saldoMes));
        }

        System.out.println("\n== TOTAL DE DESPESAS NO ANO POR CATEGORIA ==");
        for (int c = 0; c < 7; c++) {
            double somaCategoriaAno = 0;
            for (int m = 0; m < 12; m++) {
                somaCategoriaAno = somaCategoriaAno + despesasCategoriasMes[c][m];
            }
            System.out.println(" " + CATEGORIAS_DESPESAS[c] + ": R$ " + String.format("%.2f", somaCategoriaAno));
        }

        // RN11: classificacao do saldo anual
        double saldoAnual = totalReceitasAno - totalDespesasAno;
        String sitAno = "POSITIVO";
        if (saldoAnual < 0) {
            sitAno = "NEGATIVO";
        }

        System.out.println("\nTOTAL RECEITAS NO ANO: R$ " + String.format("%.2f", totalReceitasAno));
        System.out.println("TOTAL DESPESAS NO ANO: R$ " + String.format("%.2f", totalDespesasAno));
        System.out.println("Saldo anual: R$ " + String.format("%.2f", saldoAnual) + " (" + sitAno + ")");
    }

    // RF11 - Relatorio de despesas por categoria e mes (por semestre, aplica RN12)
    static void relatorioSemestral() {
        System.out.println("\n== DESPESAS POR SEMESTRE ==");
        System.out.println("1 - 1o Semestre (Jan a Jun)");
        System.out.println("2 - 2o Semestre (Jul a Dez)");
        int sem = lerInteiro("Semestre (1 ou 2): ", 1, 2);

        int ini = 0;
        int fim = 6;
        if (sem == 2) {
            ini = 6;
            fim = 12;
        }

        double[] totCol = new double[6];
        double totGeral = 0;

        for (int c = 0; c < 7; c++) {
            double totLin = 0;
            System.out.print(CATEGORIAS_DESPESAS[c] + ": ");
            int col = 0;

            for (int m = ini; m < fim; m++) {
                double g = despesasCategoriasMes[c][m];
                System.out.print(NOMES_MESES[m] + "=R$" + String.format("%.2f", g) + " | ");
                totLin = totLin + g;
                totCol[col] = totCol[col] + g;
                col = col + 1;
            }

            totGeral = totGeral + totLin;
            System.out.println("Total: R$ " + String.format("%.2f", totLin));
        }

        System.out.print("Total coluna: ");
        int col = 0;
        for (int m = ini; m < fim; m++) {
            System.out.print(NOMES_MESES[m] + "=R$" + String.format("%.2f", totCol[col]) + " | ");
            col = col + 1;
        }

        System.out.println("\nTotal geral semestre: R$ " + String.format("%.2f", totGeral));
    }

    // RF12 - Orcamento previsto x realizado (aplica RN09 e RN13)
    static void orcamentoPrevistoRealizado() {
        System.out.println("\n== ORCAMENTO PREVISTO X REALIZADO ==");
        int mes = lerInteiro("Digite o mes (1 a 12): ", 1, 12);
        int idxMes = mes - 1;
        int qtExcedido = 0;

        for (int c = 0; c < 7; c++) {
            double lim = orcamentos[c];
            double gasto = despesasCategoriasMes[c][idxMes];

            // RN09 e RN13: categoria sem limite nao calcula percentual nem diferenca
            if (lim <= 0) {
                System.out.println(CATEGORIAS_DESPESAS[c] + " | Previsto: SEM LIMITE | Realizado: R$ "
                        + String.format("%.2f", gasto) + " | Dif: - | %: - | Status: SEM LIMITE");
            } else {
                // RN13: calcula diferenca, percentual gasto e verifica se excedeu
                double dif = lim - gasto;
                double perc = (gasto / lim) * 100.0;
                String status = "DENTRO";
                if (gasto > lim) {
                    status = "EXCEDIDO";
                    qtExcedido = qtExcedido + 1;
                }
                System.out.println(CATEGORIAS_DESPESAS[c] + " | Previsto: R$ " + String.format("%.2f", lim)
                        + " | Realizado: R$ " + String.format("%.2f", gasto)
                        + " | Dif: R$ " + String.format("%.2f", dif)
                        + " | %: " + String.format("%.1f", perc) + "% | Status: " + status);
            }
        }

        System.out.println("Categorias com orcamento excedido: " + qtExcedido);
    }

    // calcula os indicadores do ano (aplica RN12 e RN14)
    static void escreverIndicadores(BufferedWriter bw) throws IOException {
        double totalReceitas = 0;
        double totalDespesas = 0;
        double[] despesasMes = new double[12];
        double[] despesasCategoria = new double[7];
        int mesesComDespesa = 0;

        for (int m = 0; m < 12; m++) {
            totalReceitas = totalReceitas + receitasPorMes[m];
            for (int c = 0; c < 7; c++) {
                despesasMes[m] = despesasMes[m] + despesasCategoriasMes[c][m];
                despesasCategoria[c] = despesasCategoria[c] + despesasCategoriasMes[c][m];
            }
            totalDespesas = totalDespesas + despesasMes[m];
            if (despesasMes[m] > 0) {
                mesesComDespesa = mesesComDespesa + 1;
            }
        }

        // RN14: media mensal considera apenas os meses com despesa maior que zero
        String linhaMedia;
        if (mesesComDespesa > 0) {
            double media = totalDespesas / mesesComDespesa;
            linhaMedia = "Media mensal de despesas: R$ " + String.format("%.2f", media);
        } else {
            linhaMedia = "Media mensal de despesas: nao calculavel (sem despesas)";
        }

        // RN14: percentual de comprometimento evita divisao por zero quando nao ha
        // receita
        String linhaComprometimento = "Percentual da receita comprometida: nao calculavel (sem receitas)";
        if (totalReceitas > 0) {
            double percentual = (totalDespesas / totalReceitas) * 100.0;
            linhaComprometimento = "Percentual da receita comprometida: " + String.format("%.1f", percentual) + "%";
        }

        String linhaMesMaior = "Mes com maior despesa: nenhum";
        if (totalDespesas > 0) {
            int maiorMes = 0;
            for (int m = 1; m < 12; m++) {
                if (despesasMes[m] > despesasMes[maiorMes]) {
                    maiorMes = m;
                }
            }
            linhaMesMaior = "Mes com maior despesa: " + NOMES_MESES[maiorMes]
                    + " (R$ " + String.format("%.2f", despesasMes[maiorMes]) + ")";
        }

        String linhaCategoriaMaior = "Categoria com maior gasto anual: nenhuma";
        if (totalDespesas > 0) {
            int maiorCategoria = 0;
            for (int c = 1; c < 7; c++) {
                if (despesasCategoria[c] > despesasCategoria[maiorCategoria]) {
                    maiorCategoria = c;
                }
            }
            double participacao = (despesasCategoria[maiorCategoria] / totalDespesas) * 100.0;
            linhaCategoriaMaior = "Categoria com maior gasto anual: " + CATEGORIAS_DESPESAS[maiorCategoria]
                    + " (R$ " + String.format("%.2f", despesasCategoria[maiorCategoria])
                    + " | " + String.format("%.1f", participacao) + "%)";
        }

        if (bw == null) {
            System.out.println(linhaMesMaior);
            System.out.println(linhaCategoriaMaior);
            System.out.println(linhaMedia);
            System.out.println(linhaComprometimento);
        } else {
            bw.write(linhaMesMaior);
            bw.newLine();
            bw.write(linhaCategoriaMaior);
            bw.newLine();
            bw.write(linhaMedia);
            bw.newLine();
            bw.write(linhaComprometimento);
            bw.newLine();
        }
    }

    // RF13 - Indicadores financeiros (aplica RN12 e RN14)
    static void indicadoresFinanceiros() {
        System.out.println("\n== INDICADORES FINANCEIROS ==");
        int ativosEncontrados = 0;

        for (int i = 0; i < qtLancamentos; i++) {
            if (ativos[i]) {
                ativosEncontrados = ativosEncontrados + 1;
            }
        }

        if (ativosEncontrados == 0) {
            System.out.println("Nao ha lancamentos ativos para calcular os indicadores.");
            return;
        }

        try {
            escreverIndicadores(null);
        } catch (IOException e) {
            System.out.println("Erro ao apresentar indicadores.");
        }
    }

    // RF14 - Exportar relatorio anual para resumo_anual.txt (aplica RN11, RN12 e
    // RN14)
    static void exportarResumoAnual() {
        System.out.println("\n== EXPORTAR RESUMO ANUAL ==");
        double totalReceitasAno = 0;
        double totalDespesasAno = 0;

        for (int m = 0; m < 12; m++) {
            totalReceitasAno = totalReceitasAno + receitasPorMes[m];
            for (int c = 0; c < 7; c++) {
                totalDespesasAno = totalDespesasAno + despesasCategoriasMes[c][m];
            }
        }

        double saldoAnual = totalReceitasAno - totalDespesasAno;
        String sitAno = "POSITIVO";
        if (saldoAnual < 0) {
            sitAno = "NEGATIVO";
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("resumo_anual.txt"))) {
            bw.write("=== RESUMO ANUAL ===");
            bw.newLine();

            for (int m = 0; m < 12; m++) {
                double despMes = 0;
                for (int c = 0; c < 7; c++) {
                    despMes = despMes + despesasCategoriasMes[c][m];
                }
                double recMes = receitasPorMes[m];
                double saldoMes = recMes - despMes;
                bw.write(NOMES_MESES[m] + " | Rec: R$ " + String.format("%.2f", recMes)
                        + " | Desp: R$ " + String.format("%.2f", despMes)
                        + " | Saldo: R$ " + String.format("%.2f", saldoMes));
                bw.newLine();
            }

            bw.newLine();
            bw.write("Total receitas: R$ " + String.format("%.2f", totalReceitasAno));
            bw.newLine();
            bw.write("Total despesas: R$ " + String.format("%.2f", totalDespesasAno));
            bw.newLine();
            bw.write("Saldo anual: R$ " + String.format("%.2f", saldoAnual) + " (" + sitAno + ")");
            bw.newLine();
            bw.newLine();

            bw.write("=== MATRIZ DESPESAS ===");
            bw.newLine();

            double[] totMes = new double[12];
            double totMat = 0;

            for (int c = 0; c < 7; c++) {
                double totLin = 0;
                bw.write(CATEGORIAS_DESPESAS[c] + ": ");
                for (int m = 0; m < 12; m++) {
                    double g = despesasCategoriasMes[c][m];
                    bw.write(NOMES_MESES[m] + "=R$" + String.format("%.2f", g) + " | ");
                    totLin = totLin + g;
                    totMes[m] = totMes[m] + g;
                }
                totMat = totMat + totLin;
                bw.write("Total: R$ " + String.format("%.2f", totLin));
                bw.newLine();
            }

            bw.write("Total mes: ");
            for (int m = 0; m < 12; m++) {
                bw.write(NOMES_MESES[m] + "=R$" + String.format("%.2f", totMes[m]) + " | ");
            }
            bw.write("Total geral: R$ " + String.format("%.2f", totMat));
            bw.newLine();
            bw.newLine();

            bw.write("=== INDICADORES ===");
            bw.newLine();
            escreverIndicadores(bw);

            System.out.println("Relatorio anual exportado para resumo_anual.txt!");
        } catch (IOException e) {
            System.out.println("Erro ao exportar: " + e.getMessage());
        }
    }

    // RF16 - Gravar dados ao encerrar o sistema (aplica RN15)
    static void salvarDados() {
        salvarLancamentos();
        salvarOrcamentos();
    }

    // RF15 - Carregar dados ao iniciar (orcamentos primeiro, depois lancamentos,
    // aplica RN16 e RN17)
    static void carregarDados() {
        carregarOrcamentos();
        carregarLancamentos();
    }

    // RF16 - Grava orcamentos.txt por sobrescrita (RN15)
    static boolean salvarOrcamentos() {
        boolean deuCerto = false;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("orcamentos.txt"))) {
            bw.write("CATEGORIA;LIMITE");
            bw.newLine();
            for (int c = 0; c < 7; c++) {
                bw.write((c + 1) + ";" + orcamentos[c]);
                bw.newLine();
            }
            deuCerto = true;
        } catch (IOException e) {
            System.out.println(
                    "Erro ao salvar orcamentos.txt. Os dados ficam so na memoria por enquanto.");
            deuCerto = false;
        }
        return deuCerto;
    }

    // RF16 - Grava lancamentos.txt por sobrescrita (RN15)
    static boolean salvarLancamentos() {
        boolean deuCerto = false;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("lancamentos.txt"))) {
            bw.write("CODIGO;TIPO;CATEGORIA;DESCRICAO;VALOR;MES;DIA;ATIVO");
            bw.newLine();
            for (int i = 0; i < qtLancamentos; i++) {
                bw.write(codigos[i] + ";" + tipos[i] + ";" + categorias[i] + ";" + descricoes[i]
                        + ";" + valores[i] + ";" + meses[i] + ";" + dias[i] + ";" + ativos[i]);
                bw.newLine();
            }
            deuCerto = true;
        } catch (IOException e) {
            System.out.println(
                    "Erro ao salvar lancamentos.txt. Os dados ficam so na memoria por enquanto.");
            deuCerto = false;
        }
        return deuCerto;
    }

    // RF15 - Le orcamentos.txt e ignora linhas invalidas (RN16)
    static void carregarOrcamentos() {
        File arquivo = new File("orcamentos.txt");
        if (!arquivo.exists()) {
            System.out.println("Aviso: orcamentos.txt nao existe ainda");
            return;
        }

        int linhasLidas = 0;
        int linhasIgnoradas = 0;
        int numeroLinha = 0;

        try (Scanner leitorArquivo = new Scanner(arquivo)) {
            while (leitorArquivo.hasNextLine()) {
                numeroLinha = numeroLinha + 1;
                String linha = leitorArquivo.nextLine().trim();

                if (linha.isEmpty()) {
                    continue;
                }
                if (numeroLinha == 1 && linha.equals("CATEGORIA;LIMITE")) {
                    continue;
                }

                // RN16: valida colunas, categoria e limite antes de carregar
                try {
                    String[] partes = linha.split(";");
                    if (partes.length != 2) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada em orcamentos.txt: faltou coluna");
                        continue;
                    }

                    int categoria = Integer.parseInt(partes[0].trim());
                    double limite = Double.parseDouble(partes[1].trim().replace(",", "."));

                    if (categoria < 1 || categoria > 7) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada em orcamentos.txt: categoria ruim");
                        continue;
                    }
                    if (limite < 0 || limite > 1000000.0) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada em orcamentos.txt: limite fora");
                        continue;
                    }

                    orcamentos[categoria - 1] = limite;
                    linhasLidas = linhasLidas + 1;
                } catch (NumberFormatException e) {
                    linhasIgnoradas = linhasIgnoradas + 1;
                    System.out.println("Linha " + numeroLinha + " ignorada em orcamentos.txt: numero invalido");
                }
            }

            System.out.println(linhasLidas + " registros carregados, " + linhasIgnoradas + " ignorados (orcamentos)");
        } catch (FileNotFoundException e) {
            System.out.println("Aviso: orcamentos.txt nao encontrado");
        }
    }

    // RF15 - Le lancamentos.txt, ignora linhas invalidas e remonta matriz e
    // proximoCodigo (RN16 e RN17)
    static void carregarLancamentos() {
        File arquivo = new File("lancamentos.txt");
        if (!arquivo.exists()) {
            System.out.println("Aviso: lancamentos.txt nao encontrado");
            return;
        }

        qtLancamentos = 0;
        proximoCodigo = 1;

        for (int m = 0; m < 12; m++) {
            receitasPorMes[m] = 0;
            for (int c = 0; c < 7; c++) {
                despesasCategoriasMes[c][m] = 0;
            }
        }

        int linhasLidas = 0;
        int linhasIgnoradas = 0;
        int numeroLinha = 0;

        try (Scanner leitorArquivo = new Scanner(arquivo)) {
            while (leitorArquivo.hasNextLine()) {
                numeroLinha = numeroLinha + 1;
                String linha = leitorArquivo.nextLine().trim();

                if (linha.isEmpty()) {
                    continue;
                }
                if (numeroLinha == 1 && linha.equals("CODIGO;TIPO;CATEGORIA;DESCRICAO;VALOR;MES;DIA;ATIVO")) {
                    continue;
                }

                // RN16: valida integridade de todos os 8 campos da linha
                try {
                    String[] partes = linha.split(";");
                    if (partes.length != 8) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: faltam campos");
                        continue;
                    }

                    int codigo = Integer.parseInt(partes[0].trim());
                    int tipo = Integer.parseInt(partes[1].trim());
                    int categoria = Integer.parseInt(partes[2].trim());
                    String descricao = partes[3].trim();
                    double valor = Double.parseDouble(partes[4].trim().replace(",", "."));
                    int mes = Integer.parseInt(partes[5].trim());
                    int dia = Integer.parseInt(partes[6].trim());
                    String ativoTexto = partes[7].trim();

                    if (codigo <= 0) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: codigo invalido");
                        continue;
                    }

                    boolean repetido = false;
                    for (int j = 0; j < qtLancamentos; j++) {
                        if (codigos[j] == codigo) {
                            repetido = true;
                            break;
                        }
                    }
                    if (repetido) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: codigo duplicado");
                        continue;
                    }

                    if (tipo != 1 && tipo != 2) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: tipo invalido");
                        continue;
                    }

                    int maxCategoria = 7;
                    if (tipo == 1) {
                        maxCategoria = 3;
                    }
                    if (categoria < 1 || categoria > maxCategoria) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: categoria invalida");
                        continue;
                    }

                    if (descricao.isEmpty()) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: descricao vazia");
                        continue;
                    }

                    if (valor <= 0 || valor > 1000000.0) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: valor invalido");
                        continue;
                    }

                    if (mes < 1 || mes > 12) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: mes invalido");
                        continue;
                    }

                    int maxDia = DIAS_POR_MES[mes - 1];
                    if (dia < 1 || dia > maxDia) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: dia invalido");
                        continue;
                    }

                    boolean ativo = false;
                    if (ativoTexto.equalsIgnoreCase("true")) {
                        ativo = true;
                    } else if (ativoTexto.equalsIgnoreCase("false")) {
                        ativo = false;
                    } else {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: ativo invalido");
                        continue;
                    }

                    if (qtLancamentos >= MAX_LANCAMENTOS) {
                        linhasIgnoradas = linhasIgnoradas + 1;
                        System.out.println("Linha " + numeroLinha + " ignorada: lotou o array");
                        continue;
                    }

                    int idx = qtLancamentos;
                    codigos[idx] = codigo;
                    tipos[idx] = tipo;
                    categorias[idx] = categoria;
                    descricoes[idx] = descricao;
                    valores[idx] = valor;
                    meses[idx] = mes;
                    dias[idx] = dia;
                    ativos[idx] = ativo;

                    qtLancamentos = qtLancamentos + 1;
                    linhasLidas = linhasLidas + 1;

                    // RN05 e RN17: garante que o proximo codigo nao repete nenhum codigo ja usado
                    // (mesmo cancelado)
                    if (codigo >= proximoCodigo) {
                        proximoCodigo = codigo + 1;
                    }

                    // RN12 e RN17: reconstroi os totalizadores e a matriz somente com registros
                    // ativos
                    if (ativo) {
                        int idxCat = categoria - 1;
                        int idxMes = mes - 1;
                        if (tipo == 1) {
                            receitasPorMes[idxMes] = receitasPorMes[idxMes] + valor;
                        } else {
                            despesasCategoriasMes[idxCat][idxMes] = despesasCategoriasMes[idxCat][idxMes] + valor;
                        }
                    }
                } catch (NumberFormatException e) {
                    linhasIgnoradas = linhasIgnoradas + 1;
                    System.out.println("Linha " + numeroLinha + " ignorada: erro de formato numerico");
                }
            }

            System.out.println(linhasLidas + " registros carregados, " + linhasIgnoradas + " ignorados (lancamentos)");
        } catch (FileNotFoundException e) {
            System.out.println("Aviso: lancamentos.txt nao encontrado");
        }
    }
}