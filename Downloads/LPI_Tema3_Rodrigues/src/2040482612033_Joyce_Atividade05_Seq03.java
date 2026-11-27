/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  *
- Disciplina : Linguagem de Programacao I                                                                       *
- Prof . Verissimo                                                                                              *
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
- Objetivo do Programa : Controle financeiro pessoal com strings e relatorios (Etapa 3 - Sequencia 3).          *
- Data - 27/11/2026                                                                                             *
- Autor : Joyce Rodrigues Teotonio                                                                              *
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;

public class 2040482612033_Joyce_Atividade05_Seq03 {

    // limites e listas fixas do sistema
    static final int MAX_LANCAMENTOS = 300;
    static final String[] NOMES_MESES = { "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto",
            "Setembro", "Outubro", "Novembro", "Dezembro" };
    static final int[] DIAS_POR_MES = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    static final String[] CATEGORIAS_RECEITAS = { "Salario", "Renda extra", "Outras receitas" };
    static final String[] CATEGORIAS_DESPESAS = { "Moradia", "Alimentacao", "Transporte", "Saude", "Educacao", "Lazer",
            "Outras despesas" };

    // arrays paralelos pra guardar cada lancamento na mesma posicao i
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

    // matriz de categoria x mes e arrays de totais
    static double[] orcamentos = new double[7];
    static double[] receitasPorMes = new double[12];
    static double[][] despesasCategoriasMes = new double[7][12];
    static Scanner sc = new Scanner(System.in);

    // le numero inteiro dentro de um intervalo sem deixar travar se digitar letra
    static int lerInteiro(String msg, int min, int max) {
        int numero = 0;
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            String linha = sc.nextLine().trim();
            try {
                numero = Integer.parseInt(linha);
                if (numero >= min && numero <= max) {
                    ok = true;
                } else {
                    System.out.println("Erro: digite entre " + min + " e" + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite apenas numeros inteiros");
            }
        }
        return numero;
    }

    // le valor com virgula ou ponto e valida se pode zero ou nao
    static double lerDecimal(String msg, double min, double max, boolean podeZero) {
        double valor = 0;
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            String texto = sc.nextLine().trim().replace(",", ".");
            try {
                valor = Double.parseDouble(texto);
                if (podeZero) {
                    if (valor >= min && valor <= max) {
                        ok = true;
                    } else {
                        System.out.println("Erro: tem q ficar entre " + min + " e " + max);
                    }
                } else {
                    if (valor > min && valor <= max) {
                        ok = true;
                    } else {
                        System.out.println("Erro: o valor deve ser maior que " + min + " e até" + max);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite um valor numerico valido");
            }
        }
        return valor;
    }

    // le texto nao vazio e barra ponto e virgula pra nao baguncar o txt
    static String lerTexto(String msg, boolean semPontoVirgula) {
        String texto = "";
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            texto = sc.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("Erro: nao pode ficar vazio");
            } else {
                if (semPontoVirgula && texto.contains(";")) {
                    System.out.println("Erro: não use ponto e virgula (;)");
                } else {
                    ok = true;
                }
            }
        }
        return texto;
    }

    // pergunta S ou N ate responder direito
    static boolean pedeSimNao(String msg) {
        boolean resultado = false;
        boolean ok = false;
        while (ok == false) {
            System.out.print(msg);
            String resp = sc.nextLine().trim().toUpperCase();
            if (resp.equals("S")) {
                resultado = true;
                ok = true;
            } else {
                if (resp.equals("N")) {
                    resultado = false;
                    ok = true;
                } else {
                    System.out.println("Erro: digite S ou N");
                }
            }
        }
        return resultado;
    }

    // carrega os arquivos no inicio, roda o menu e salva ao sair
    public static void main(String[] args) {
        int opcao = -1;
        do {
            System.out.println("\n=== MINHAS FINANÇAS ===- CONTROLE FINANCEIRO PESSOAL ==");
            System.out.println("1 - Registrar receita");
            System.out.println("2 - Registrar despesas");
            System.out.println("5 - Listar lançamentos");
            System.out.println("6 - Consultar lançamento por código");
            System.out.println("7 - Pesquisar despesas por categoria");
            System.out.println("8 - Extrato mensal");
            System.out.println("9 - Definir orçamento mensal por categoria");
            System.out.println("10 - resumo financeiro anual");
            System.out.println("11 - Despesas por categoria e mês");
            System.out.println("12 - Orçamento previsto x realizado");
            System.out.println("13 - Indicadores financeiros");
            System.out.println("0 - Sair");
            System.out.println("Escolha uma opção: ");
            try {
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números.");
                continue;
            }

            if (opcao == 0) {
                System.out.println("Adeus");
            } else if (opcao == 1) {
                registrarReceita();
            } else if (opcao == 2) {
                registrarDespesa();
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
            } else {
                System.out.println("Opção inválida");
            }
        } while (opcao != 0);
        sc.close();
    }

    // RF01 - Registrar receita (aplica RN01 a RN06)
    static void registrarReceita() {
        System.out.println("\n== REGISTRAR RECEITA ===");
        if (qtLancamentos >= MAX_LANCAMENTOS) {
            System.out.println("Erro: limite máximo de lançamentos atingidos.");
            return;
        }
        String descricao = lerTexto("Descrição: ", true);
        System.out.println("Categorias: 1- Salario | 2- Renda extra | 3-Outras receitas");
        int categoria = lerInteiro("Categoria (1 a 3): ", 1, 3);
        double valor = lerDecimal("Valor (R$): ", 0, 1000000.0, false);
        int mes = lerInteiro("Mes (1 a 12): ", 1, 12);
        int maxDias = DIAS_POR_MES[mes - 1];
        int dia = lerInteiro("Dia (1 a " + maxDias + "): ", 1, maxDias);

        // guarda nos arrays paralelos e soma no total de receitas do mes
        int i = qtLancamentos;
        codigos[i] = proximoCodigo;
        descricoes[i] = descricao;
        tipos[i] = 1;
        categorias[i] = categoria;
        valores[i] = valor;
        meses[i] = mes;
        dias[i] = dia;
        ativos[i] = true;

        receitasPorMes[mes - 1] = receitasPorMes[mes - 1] + valor;
        qtLancamentos = qtLancamentos + 1;
        proximoCodigo = proximoCodigo + 1;

        System.out.println("Receita registrada com sucesso! Codigo: " + codigos[i]);
    }

    // RF02 - Registrar despesa (aplica RN01 a RN06, RN10 e RN11)
    static void registrarDespesa() {
        System.out.println("=REGISTRAR DESPESA =");
        if (qtLancamentos >= MAX_LANCAMENTOS) {
            System.out.println("Erro: limite MAXIMO DE LANCAMENTOS ATINGIDOS");
            return;
        }
        String descricao = lerTexto("Descricao: ", true);
        System.out.println(
                "Categorias: 1- Moradia | 2- Alimentacao | 3- Transporte | 4- Saude | 5- Educacao | 6- Lazer | 7- Outras despesas");
        int categoria = lerInteiro("Categoria (1 a 7): ", 1, 7);
        double valor = lerDecimal("Valor (R$: ", 0, 1000000.0, false);
        int mes = lerInteiro("Mes (1 a 12): ", 1, 12);
        int maxDias = DIAS_POR_MES[mes - 1];
        int dia = lerInteiro("Dia (1 a " + maxDias + "): ", 1, maxDias);

        int i = qtLancamentos;
        codigos[i] = proximoCodigo;
        descricoes[i] = descricao;
        tipos[i] = 2;
        categorias[i] = categoria;
        valores[i] = valor;
        meses[i] = mes;
        dias[i] = dia;
        ativos[i] = true;

        // atualiza a matriz de gastos na categoria e mes
        despesasCategoriasMes[categoria - 1][mes - 1] = despesasCategoriasMes[categoria - 1][mes - 1] + valor;
        qtLancamentos = qtLancamentos + 1;
        proximoCodigo = proximoCodigo + 1;

        System.out.println("Despesa registrada com sucesso! Codigo: " + codigos[i]);

        // aplicacao da RN10: avisa se passou do limite do orcamento
        double limite = orcamentos[categoria - 1];
        double gastoCategoriaMes = despesasCategoriasMes[categoria - 1][mes - 1];
        if (limite > 0 && gastoCategoriaMes > limite) {
            System.out.println("ALERTA: Orcamento de " + CATEGORIAS_DESPESAS[categoria - 1] + " excedido em "
                    + NOMES_MESES[mes-1]+ "!");
        }

        // aplicacao da RN11: avisa se o saldo do mes ficou negativo
        double totalDespesasMes = 0;
        for (int c = 0; c < 7; c++) {
            totalDespesasMes = totalDespesasMes + despesasCategoriasMes[c][mes - 1];
        }
        double saldoMes = receitasPorMes[mes - 1] - totalDespesasMes;
        if (saldoMes < 0) {
            System.out.println("ALERTA: saldo de " + NOMES_MESES[mes - 1] + " ficou NEGATIVO: R$ "
                    + String.format("%.2f", saldoMes));
        }
    }

    // RF05 - Listar lancamentos ativos
    static void listarLancamentos() {
        System.out.println("\n LISTA DE LANCAMENTOS =");
        double totalReceitas = 0;
        double totalDespesas = 0;
        int cont = 0;

        for (int i = 0; i < qtLancamentos; i++) {
            if (ativos[i]) {
                String tipoTexto;
                String nomeCat;

                if (tipos[i] == 1) {
                    tipoTexto = "Receita";
                    nomeCat = CATEGORIAS_RECEITAS[categorias[i] - 1];
                    totalReceitas = totalReceitas + valores[i];
                } else {
                    tipoTexto = "Despesa";
                    nomeCat = CATEGORIAS_DESPESAS[categorias[i] - 1];
                    totalDespesas = totalDespesas + valores[i];
                }
                System.out.println(codigos[i] + " | " + dias[i] + "/" + meses[i] + " | " + tipoTexto + " | " + nomeCat
                        + " |" + descricoes[i] + " | R$ " + String.format("%.2f", valores[i]));
                cont = cont + 1;
            }
        }

        if (cont == 0) {
            System.out.println("Nenhum lançamento cadastrado.");
        } else {
            double saldo = totalReceitas - totalDespesas;
            System.out.println("\n TOTAL receitas: R$" + String.format("%.2f", totalReceitas));
            System.out.println("Total despesas: R$ " + String.format("%.2f", totalDespesas));
            System.out.println("Saldo R$ " + String.format("%.2f", saldo));
        }
    }

    // RF06 - Consultar lancamento por codigo (mostra ate os cancelados)
    static void consultarPorCodigo() {
        System.out.println("\n= CONSULTAR POR CODIGO =");
        int codBusca = lerInteiro("Digite o codigo: ", 1, 999999);
        for (int i = 0; i < qtLancamentos; i++) {
            if (codigos[i] == codBusca) {
                String tipoTexto = "Despesa";
                String nomeCat = CATEGORIAS_DESPESAS[categorias[i] - 1];
                if (tipos[i] == 1) {
                    tipoTexto = "Receita";
                    nomeCat = CATEGORIAS_RECEITAS[categorias[i] - 1];
                }
                String sit = "Cancelado";
                if (ativos[i]) {
                    sit = "Ativo";
                }
                System.out.println("Codigo: " + codigos[i] + " | " + tipoTexto + " | " + nomeCat);
                System.out.println("Descricao: " + descricoes[i] + " | Valor: R$" + String.format("%.2f", valores[i]));
                System.out.println("Data: " + dias[i] + "/" + meses[i] + " | Status: " + sit);
                return;
            }
        }
        System.out.println("Lancamento nao encontrado");
    }

    // RF07 - Pesquisar despesas por categoria
    static void pesquisarDespesasCategoria() {
        System.out.println("\n= PESQUISAR DESPESAS POR CATEGORIA =");
        System.out.println(
                "1- moradia | 2- alimentacao | 3- transporte | 4- saude | 5- educacao | 6- lazer | 7- outras despesas");
        int catEscolhida = lerInteiro("Categoria (1 a 7): ", 1, 7);

        double soma = 0;
        int achados = 0;
        for (int i = 0; i < qtLancamentos; i++) {
            if (ativos[i] && tipos[i] == 2 && categorias[i] == catEscolhida) {
                System.out.println(codigos[i] + " | " + dias[i] + "/" + meses[i] + " | " + descricoes[i]
                        + " | R$" + String.format("%.2f", valores[i]));
                soma = soma + valores[i];
                achados = achados + 1;
            }
        }

        if (achados == 0) {
            System.out.println("Nenhuma despesa nessa categoria.");
        } else {
            double media = soma / achados;
            System.out.println("Total: R$" + String.format("%.2f", soma));
            System.out.println("Media: R$" + String.format("%.2f", media));
        }
    }

    // RF08 - Emitir extrato mensal (aplica RN11)
    static void extratoMensal() {
        System.out.println("\n= EXTRATO MENSAL =");
        int mes = lerInteiro("Digite o mes (1 a 12): ", 1, 12);
        System.out.println("\nEXTRATO DE " + NOMES_MESES[mes - 1].toUpperCase());

        double recMes = 0;
        double despMes = 0;
        int qt = 0;

        for (int i = 0; i < qtLancamentos; i++) {
            if (ativos[i] && meses[i] == mes) {
                String tipoTexto = "Despesa";
                String nomeCat = CATEGORIAS_DESPESAS[categorias[i] - 1];
                if (tipos[i] == 1) {
                    tipoTexto = "Receita";
                    nomeCat = CATEGORIAS_RECEITAS[categorias[i] - 1];
                    recMes = recMes + valores[i];
                } else {
                    despMes = despMes + valores[i];
                }
                System.out.println(codigos[i] + " | Dia " + dias[i] + " | " + tipoTexto + " | " + nomeCat
                        + " | " + descricoes[i] + " | R$" + String.format("%.2f", valores[i]));
                qt = qt + 1;
            }
        }

        if (qt == 0) {
            System.out.println("Nenhum lancamento neste mes.");
            return;
        }

        double saldo = recMes - despMes;
        String situacao = "POSITIVO";
        if (saldo < 0) {
            situacao = "NEGATIVO";
        }
        System.out.println("Receitas: R$" + String.format("%.2f", recMes));
        System.out.println("Despesas: R$" + String.format("%.2f", despMes));
        System.out.println("Saldo: R$" + String.format("%.2f", saldo) + " (" + situacao + ")");
    }

    // RF09 - Definir orcamento mensal (aplica RN09)
    static void definirOrcamento() {
        System.out.println("\n DEFINIR ORCAMENTO POR CATEGORIA");
        for (int c = 0; c < 7; c++) {
            System.out.println((c + 1) + " - " + CATEGORIAS_DESPESAS[c]);
        }
        int categoria = lerInteiro("Escolha a categoria ( 1 a 7): ", 1, 7);
        double limite = lerDecimal("Limite mensal em R$ (0 para remover limite): ", 0, 1000000.0, true);

        orcamentos[categoria - 1] = limite;
        System.out.println("Orcamento de " + CATEGORIAS_DESPESAS[categoria - 1] + " definido para R$ "
                + String.format("%.2f", limite));
        System.out.println("\nOrcamentos vigentes:");
        for (int c = 0; c < 7; c++) {
            if (orcamentos[c] > 0) {
                System.out.println(" " + CATEGORIAS_DESPESAS[c] + ": R$" + String.format("%.2f", orcamentos[c]));
            } else {
                System.out.println(" " + CATEGORIAS_DESPESAS[c] + ": SEM LIMITE");
            }
        }
    }

    // RF10 - Resumo financeiro anual (aplica RN11 e RN12)
    static void relatorioAnual() {
        System.out.println("\n=RELATORIO ANUAL =");
        double totalReceitasAno = 0;
        double totalDespesasAno = 0;

        System.out.println("=MES A MES=");
        for (int m = 0; m < 12; m++) {
            double despMes = 0;
            for (int c = 0; c < 7; c++) {
                despMes = despMes + despesasCategoriasMes[c][m];
            }
            double recMes = receitasPorMes[m];
            double saldoMes = recMes - despMes;
            totalReceitasAno = totalReceitasAno + recMes;
            totalDespesasAno = totalDespesasAno + despMes;

            System.out.println(NOMES_MESES[m] + ">Receitas: R$ " + String.format("%.2f", recMes)
                    + "| Despesas: R$ " + String.format("%.2f", despMes) + " | Saldo: R$"
                    + String.format("%.2f", saldoMes));
        }
        System.out.println("\n=TOTAL DE DESPESAS NO ANO POR CATEGORIA-");
        for (int c = 0; c < 7; c++) {
            double somaCategoriaAno = 0;
            for (int m = 0; m < 12; m++) {
                somaCategoriaAno = somaCategoriaAno + despesasCategoriasMes[c][m];
            }
            System.out.println(" " + CATEGORIAS_DESPESAS[c] + ": R$ " + String.format("%.2f", somaCategoriaAno));
        }
        double saldoAnual = totalReceitasAno - totalDespesasAno;
        String sitAno = "POSITIVO";
        if (saldoAnual < 0) {
            sitAno = "NEGATIVO";
        }
        System.out.println("\n TOTAL RECEITAS NO ANO: R$" + String.format("%.2f", totalReceitasAno));
        System.out.println("TOTAL DESPESAS NO ANO: R$" + String.format("%.2f", totalDespesasAno));
        System.out.println("Saldo anual: R$" + String.format("%.2f", saldoAnual) + " (" + sitAno + ")");
    }

    // RF11 - Relatorio de despesas por categoria e mes (por semestre)
    static void relatorioSemestral() {
        System.out.println("\n= DESPESAS POR SEMESTRE =");
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

        // percorre as linhas (categorias) e colunas (meses do semestre) da matriz
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
            System.out.println("Total: R$" + String.format("%.2f", totLin));
        }

        System.out.print("Total coluna: ");
        int col = 0;
        for (int m = ini; m < fim; m++) {
            System.out.print(NOMES_MESES[m] + "=R$" + String.format("%.2f", totCol[col]) + " | ");
            col = col + 1;
        }
        System.out.println("\nTotal geral semestre: R$" + String.format("%.2f", totGeral));
    }

    // RF12 - Relatorio de orcamento previsto versus realizado (aplica RN09 e RN13)
    static void orcamentoPrevistoRealizado() {
        System.out.println("\n= ORCAMENTO PREVISTO X REALIZADO =");
        int mes = lerInteiro("Digite o mes (1 a 12): ", 1, 12);
        int qtExcedido = 0;

        for (int c = 0; c < 7; c++) {
            double lim = orcamentos[c];
            double gasto = despesasCategoriasMes[c][mes - 1];
            if (lim <= 0) {
                System.out.println(CATEGORIAS_DESPESAS[c] + " | Previsto: SEM LIMITE | Realizado: R$"
                        + String.format("%.2f", gasto) + " | Dif: - | %: - | Status: SEM LIMITE");
            } else {
                double dif = lim - gasto;
                double perc = (gasto / lim) * 100.0;
                String status = "DENTRO";
                if (gasto > lim) {
                    status = "EXCEDIDO";
                    qtExcedido = qtExcedido + 1;
                }
                System.out.println(CATEGORIAS_DESPESAS[c] + " | Previsto: R$" + String.format("%.2f", lim)
                        + " | Realizado: R$" + String.format("%.2f", gasto)
                        + " | Dif: R$" + String.format("%.2f", dif)
                        + " | %: " + String.format("%.1f", perc) + "% | Status: " + status);
            }
        }
        System.out.println("Categorias com orcamento excedido: " + qtExcedido);
    }

    // calcula os indicadores do ano (reaproveitado na tela e no arquivo do RF14)
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

        // media considerando so os meses que tiveram alguma despesa
        String linhaMedia;
        if (mesesComDespesa > 0) {
            double media = totalDespesas / mesesComDespesa;
            linhaMedia = "Media mensal de despesas: R$" + String.format("%.2f", media);
        } else {
            linhaMedia = "Media mensal de despesas: nao calculavel (sem despesas)";
        }

        // aplicacao da RN14: percentual da receita comprometida
        String linhaComprometimento = "Percentual da receita comprometida: nao calculavel (sem receitas)";
        if (totalReceitas > 0) {
            double percentual = totalDespesas / totalReceitas * 100.0;
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
                    + " (R$" + String.format("%.2f", despesasMes[maiorMes]) + ")";
        }

        String linhaCategoriaMaior = "Categoria com maior gasto anual: nenhuma";
        if (totalDespesas > 0) {
            int maiorCategoria = 0;
            for (int c = 1; c < 7; c++) {
                if (despesasCategoria[c] > despesasCategoria[maiorCategoria]) {
                    maiorCategoria = c;
                }
            }
            double participacao = despesasCategoria[maiorCategoria] / totalDespesas * 100.0;
            linhaCategoriaMaior = "Categoria com maior gasto anual: " + CATEGORIAS_DESPESAS[maiorCategoria]
                    + " (R$" + String.format("%.2f", despesasCategoria[maiorCategoria])
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

    // RF13 - Indicadores financeiros (aplica RN14)
    static void indicadoresFinanceiros() {
        System.out.println("\n= INDICADORES FINANCEIROS =");
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
}
