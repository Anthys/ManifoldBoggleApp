dico_trad = {
    "à":"a",
    "â":"a",
    "ä":"a",
    "é":"e",
    "ê":"e",
    "è":"e",
    "ë":"e",
    "î":"i",
    "ï":"i",
    "ô":"o",
    "ö":"o",
    "û":"u",
    "ü":"u",
    "ù":"u",
    "ç": "c",
    "œ":"oe",
    "-":""
}


def clean_word(word):
    new_word = []
    for i in range(len(word)):
        new_word.append(dico_trad[word[i]])
    return "".join(new_word)

def main():
    common_letters = "azertyuiopqsdfghjklmwxcvbn"

    for i in common_letters:
        dico_trad[i] = i
        dico_trad[chr(ord(i)-32)] = i

    print(dico_trad)

    liste_mots, liste_prefix = create_lists("liste.txt")
    write_list(liste_mots, liste_prefix)

def create_lists(in_file):
    liste_mots_prev = open(in_file, encoding="utf-8")
    liste_mots_next = set()
    liste_prefix = set()
    for i,line in enumerate(liste_mots_prev):
        mot = line.split()[0]
        mot = clean_word(mot)
        if not mot in liste_mots_next:
            liste_mots_next.add(mot)
        for i in range(1,len(mot)+1):
            if not mot[:i] in liste_prefix:
                liste_prefix.add(mot[:i])
    liste_mots_prev.close()

    liste_prefix = list(liste_prefix)
    liste_prefix = sorted(liste_prefix, key=len)
    liste_prefix = sorted(liste_prefix)
    
    liste_mots_next = list(liste_mots_next)
    liste_mots_next = sorted(liste_mots_next, key=len)
    liste_mots_next = sorted(liste_mots_next)
    return liste_mots_next, liste_prefix

def write_list(liste_mots, liste_prefix):
    out_file = open("OUT_WORDS.txt", "w+")
    txt = "\n".join(liste_mots)
    out_file.write(txt)
    out_file.close()

    out_file = open("OUT_PREFIX.txt", "w+")
    txt = "\n".join(liste_prefix)
    out_file.write(txt)
    out_file.close()

if __name__ == "__main__":
    main()
