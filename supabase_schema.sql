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

-- Suppression réservée à l'ADMIN connecté (Supabase Auth).
-- Les visiteurs anonymes (clé anon) ne peuvent PAS supprimer.
drop policy if exists "potholes_delete_admin" on public.potholes;
create policy "potholes_delete_admin" on public.potholes
    for delete using (auth.role() = 'authenticated');

-- ============================================================================
-- SIGNALEMENTS D'ABUS (photo non conforme, image de personne, faux trou...)
-- ============================================================================
alter table public.potholes add column if not exists report_count int not null default 0;

create table if not exists public.reports (
    id         uuid primary key default gen_random_uuid(),
    pothole_id uuid references public.potholes(id) on delete cascade,
    reason     text,
    created_at timestamptz default now()
);

alter table public.reports enable row level security;

-- Seul l'admin connecté peut lire les motifs de signalement.
drop policy if exists "reports_select_admin" on public.reports;
create policy "reports_select_admin" on public.reports
    for select using (auth.role() = 'authenticated');

-- Fonction appelable par tout le monde (anon) pour signaler un trou :
-- insère le motif et incrémente le compteur, de façon atomique.
create or replace function public.report_pothole(p_id uuid, p_reason text)
returns void
language sql
security definer
set search_path = public
as $$
    insert into public.reports(pothole_id, reason) values (p_id, p_reason);
    update public.potholes set report_count = report_count + 1 where id = p_id;
$$;

grant execute on function public.report_pothole(uuid, text) to anon, authenticated;
