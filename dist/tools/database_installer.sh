#!/bin/bash
# L2jOrion MariaDB Installer - Linux Version
# Adaptado para MariaDB com Logs e Backup Automático

LOG_FILE="install_log.txt"
echo "--- Iniciando Instalação: $(date) ---" > "$LOG_FILE"

trap finish 2

configure() {
    echo "#############################################"
    echo "# L2jOrion: Área de Configuração            #"
    echo "#############################################"
    
    # Busca binários do MariaDB
    MYSQLPATH=$(which mariadb 2>/dev/null)
    MYSQLDUMPPATH=$(which mariadb-dump 2>/dev/null)

    if [ -z "$MYSQLPATH" ]; then
        echo "Binários do MariaDB não encontrados no PATH!"
        exit 1
    fi

    echo -ne "\nHost do MariaDB (default localhost): "
    read LSDBHOST
    LSDBHOST=${LSDBHOST:-localhost}

    echo -ne "Nome da Database (default l2_database): "
    read LSDB
    LSDB=${LSDB:-l2_database}

    echo -ne "Usuário (default root): "
    read LSUSER
    LSUSER=${LSUSER:-root}

    echo -ne "Senha do usuário $LSUSER (não será exibida): "
    stty -echo
    read LSPASS
    stty echo
    echo ""

    save_config "$1"
}

save_config() {
    CONF=${1:-database_installer.rc"}
    cat <<EOF > "$CONF"
MYSQLPATH=$MYSQLPATH
MYSQLDUMPPATH=$MYSQLDUMPPATH
LSDBHOST=$LSDBHOST
LSDB=$LSDB
LSUSER=$LSUSER
LSPASS=$LSPASS
EOF
    chmod 600 "$CONF"
    echo "Configuração salva em $CONF"
}

load_config() {
    CONF=${1:-database_installer.rc}
    if [ -f "$CONF" ]; then
        source "$CONF"
    else
        configure "$CONF"
    fi
}

make_backup() {
    echo "[!] Criando backup de segurança..."
    BACKUP_NAME="backup_$(date +%Y%m%d_%H%M%S).sql"
    $MYSQLDUMPPATH -h "$LSDBHOST" -u "$LSUSER" --password="$LSPASS" "$LSDB" > "$BACKUP_NAME" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "[OK] Backup criado: $BACKUP_NAME"
        echo "[LOG] Backup realizado com sucesso." >> "$LOG_FILE"
    else
        echo "[!] Aviso: Não foi possível fazer backup (DB nova?)."
    fi
}

run_sql_dir() {
    local DIR=$1
    if [ -d "$DIR" ]; then
        echo "[+] Processando pasta: $DIR"
        for sql_file in $(find "$DIR" -maxdepth 1 -name "*.sql" | sort); do
            echo "    -> Importando: $(basename "$sql_file")"
            $MYSQLPATH -h "$LSDBHOST" -u "$LSUSER" --password="$LSPASS" "$LSDB" < "$sql_file" 2>> "$LOG_FILE"
            if [ $? -eq 0 ]; then
                echo "[SUCCESS] $(basename "$sql_file")" >> "$LOG_FILE"
            else
                echo "[ERROR] Falha no arquivo $sql_file" >> "$LOG_FILE"
            fi
        done
    fi
}

ask_type() {
    clear
    echo "==============================================="
    echo "   L2jOrion MariaDB Installer - MENU"
    echo "==============================================="
    echo "DB: $LSDB | HOST: $LSDBHOST"
    echo "-----------------------------------------------"
    echo "Escolha o tipo de instalação:"
    echo "f) FULL (Apaga tudo e instala do zero)"
    echo "u) UPGRADE (Apenas atualiza sem apagar dados)"
    echo "q) SAIR"
    echo -ne "\nOpção: "
    read OPT
    case "$OPT" in
        f|F) 
            make_backup
            echo "Limpando e Instalando Full..."
            run_sql_dir "./sql/login"
            run_sql_dir "./sql/server"
            run_sql_dir "./sql/server/custom"
            finish ;;
        u|U)
            make_backup
            echo "Rodando Upgrades..."
            run_sql_dir "./sql/server/updates"
            run_sql_dir "./sql/login/updates"
            finish ;;
        q|Q) finish ;;
        *) ask_type ;;
    esac
}

finish() {
    echo -e "\n-----------------------------------------------"
    echo "Processo Finalizado."
    echo "Verifique o log em: $LOG_FILE"
    echo "Visite: https://www.l2jorion.com"
    exit 0
}

# Início do Script
clear
load_config "$1"
ask_type