-- ============================================================================
-- Schéma Supabase pour Hna Hofra.
-- À exécuter UNE FOIS : Supabase > votre projet > SQL Editor > coller > Run.
-- L'app est anonyme (aucune connexion) : lecture, signalement et passage à
-- "réparé" sont autorisés pour tout le monde (clé anon). La suppression est
-- interdite ; les trous réparés depuis > 10 jours sont masqués côté app.
-- ============================================================================

create table if not exists public.potholes (
    id            uuid primary key default gen_random_uuid(),
    reporter_name text not null,
    state         text not null default 'OPEN',   -- 'OPEN' ou 'REPAIRED'
    lat           double precision not null,
    lng           double precision not null,
    image_url     text,
    date          bigint not null,                 -- date de la dernière photo (epoch ms)
    created_at    timestamptz default now()
);

alter table public.potholes enable row level security;

-- Lecture publique
drop policy if exists "potholes_select" on public.potholes;
create policy "potholes_select" on public.potholes
    for select using (true);

-- Insertion publique (nouveau signalement)
drop policy if exists "potholes_insert" on public.potholes;
create policy "potholes_insert" on public.potholes
    for insert with check (true);

-- Mise à jour publique (passage à "réparé")
drop policy if exists "potholes_update" on public.potholes;
create policy "potholes_update" on public.potholes
    for update using (true) with check (true);
