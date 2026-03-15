--
-- PostgreSQL database dump
--

\restrict TaBaWob9HjNX02Sro5aTu2vgYirTfEzQjmddud5KcOIZryfIj4VBh6hBZXQjYlH

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ 
BEGIN
  NEW.updated_at = NOW( );
  RETURN NEW;
END;
$$;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: event; Type: TABLE; Schema: public
--

CREATE TABLE public.event (
    id bigint CONSTRAINT events_id_not_null NOT NULL,
    type character varying(255) CONSTRAINT events_type_not_null NOT NULL,
    payload jsonb,
    status character varying(255) DEFAULT 'pending'::character varying,
    retry_count integer DEFAULT 0,
    error_msg text,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP,
    next_run_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP
);

--
-- Name: COLUMN event.id; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.id IS '事件的唯一标识符。';


--
-- Name: COLUMN event.type; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.type IS '事件的类型，用于提供给 worker 分配任务。';


--
-- Name: COLUMN event.payload; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.payload IS '事件所需的参数。';


--
-- Name: COLUMN event.status; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.status IS '事件的状态，合法值为 pending、succeed、failed 等。';


--
-- Name: COLUMN event.retry_count; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.retry_count IS '事件重试次数。';


--
-- Name: COLUMN event.error_msg; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.error_msg IS '事件产生错误时的错误消息。';


--
-- Name: COLUMN event.created_at; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.created_at IS '事件创建时的时间戳。';


--
-- Name: COLUMN event.next_run_at; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.next_run_at IS '事件下一次尝试的时间戳。';


--
-- Name: COLUMN event.updated_at; Type: COMMENT; Schema: public
--

COMMENT ON COLUMN public.event.updated_at IS '事件更新的时间戳。';


--
-- Name: events_id_seq; Type: SEQUENCE; Schema: public
--

ALTER TABLE public.event ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: event events_pkey; Type: CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: index_events_status_pending; Type: INDEX; Schema: public
--

CREATE INDEX index_events_status_pending ON public.event USING btree (status, next_run_at);


--
-- Name: event trigger_update_events_updated_at; Type: TRIGGER; Schema: public
--

CREATE TRIGGER trigger_update_events_updated_at BEFORE UPDATE ON public.event FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- PostgreSQL database dump complete
--

\unrestrict TaBaWob9HjNX02Sro5aTu2vgYirTfEzQjmddud5KcOIZryfIj4VBh6hBZXQjYlH

